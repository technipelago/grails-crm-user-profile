/*
 * Copyright (c) 2016 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.security

import grails.plugins.crm.core.TenantUtils
import grails.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletResponse

/**
 * Created by goran on 2014-03-16.
 */
class UserController {

    static allowedMethods = [list: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def crmSecurityService
    def crmAccountService
    def crmUserService
    def resetPasswordService

    def index() {
        redirect action: 'profile'
    }

    /**
     * Edit user settings.
     *
     * @param cmd command object
     * @return render settings.gsp
     */
    @Transactional
    def profile(UserSettingsCommand cmd) {
        def user = crmSecurityService.getCurrentUser()
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }

        cmd.locale = RequestContextUtils.getLocale(request)

        if (request.post) {
            if (cmd.validate()) {

                cmd.bindCrmUser(user)

                if (user.save()) {
                    if (resetPasswordService) {
                        def username = user.username
                        def existing = resetPasswordService.getQuestionsForUser(username)
                        10.times {
                            def q = params['q[' + it + ']']
                            def a = params['a[' + it + ']']
                            if (q && a) {
                                if (a != '**********') {
                                    resetPasswordService.addAnswer(username, q, a)
                                }
                                existing.remove(q)
                            }
                        }
                        for (q in existing) {
                            resetPasswordService.removeAnswer(username, q)
                        }
                    }

                    flash.success = message(code: 'crmSettings.updated.message', default: "Settings updated",
                            args: [message(code: 'crmUser.label', default: 'User'), user.name])
                } else {
                    log.error("Failed to save settings for user [${user.username}] ${user.errors}")
                    flash.error = message(code: 'crmSettings.not.updated.message', default: "Settings could not be updated")
                }
            }
        } else {
            cmd.setFromUser(user)
            cmd.validate()
            cmd.clearErrors()
        }

        return getModel(cmd)
    }

    private Map getModel(UserSettingsCommand cmd) {
        def crmUser = crmSecurityService.getCurrentUser()
        def options = crmUser.option
        def username = crmUser.username
        def questions = resetPasswordService?.getAvailableQuestions()
        def answers = resetPasswordService?.getQuestionsForUser(username)
        def tenants = crmSecurityService.getTenants(username)
        def timezones = TimeZone.getAvailableIDs().findAll { it.contains("Europe") }.collect {
            TimeZone.getTimeZone(it)
        }
        def currencies = ['SEK', 'NOK', 'EUR', 'GBP', 'USD'].collect { Currency.getInstance(it) }
        return [cmd      : cmd, crmUser: crmUser,
                options  : options, tenants: tenants,
                roles    : crmUser.roles,
                questions: questions, answers: answers,
                timezones: timezones, currencies: currencies]
    }

    def list() {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def result
        try {
            result = crmUserService.list(params, params)
            [result: result, totalCount: result.totalCount]
        } catch (Exception e) {
            flash.error = e.message
            [result: [], totalCount: 0]
        }
    }

    @Transactional
    def create() {
        def crmUser = new CrmUser(status: CrmUser.STATUS_ACTIVE)

        switch (request.method) {
            case 'GET':
                return [crmUser: crmUser]
            case 'POST':
                bindData(crmUser, params, [include: CrmUser.BIND_WHITELIST])
                if (crmUser.hasErrors()) {
                    render view: 'create', model: [crmUser: crmUser]
                    return
                }

                params.defaultTenant = 1L

                if (params.password1) {
                    if (params.password1 == params.password2) {
                        params.password = params.password1
                        params.status = CrmUser.STATUS_ACTIVE
                    } else {
                        flash.error = message(code: 'settings.password.not.equal', default: "Passwords were not equal")
                        render view: 'create', model: [crmUser: crmUser]
                        return
                    }
                } else {
                    // Assign random 16 character password and make sure the user cannot login.
                    params.password = UUID.randomUUID().toString().substring(0, 15)
                    params.status = CrmUser.STATUS_NEW
                }

                try {
                    crmUser = crmSecurityService.createUser(params.subMap(['username', 'email', 'name', 'company', 'telephone', 'defaultTenant', 'status', 'password']))
                    for (t in CrmTenant.list()) {
                        crmSecurityService.addUserRole(crmUser, 'guest', null, t.id)
                    }
                    def role = params.role
                    if (role && (role != 'guest')) {
                        crmSecurityService.addUserRole(crmUser, role, null, TenantUtils.tenant)
                    }
                    flash.success = message(code: 'crmUser.created.message', args: [message(code: 'crmUser.label', default: 'User'), crmUser.toString()])
                    redirect action: 'list'
                } catch (Exception e) {
                    flash.error = e.message
                    render view: 'create', model: [crmUser: crmUser]
                }
                break
        }
    }

    @Transactional
    def edit() {
        def crmUser = CrmUser.get(params.id)
        if (!crmUser) {
            flash.error = message(code: 'crmUser.not.found.message', args: [message(code: 'crmUser.label', default: 'User'), params.id])
            redirect action: 'list'
            return
        }
        // If this user owns an account with tenants, it cannot be deleted.
        def deleteOk = true
        def accounts = CrmAccount.findAllByUser(crmUser)
        for (a in accounts) {
            if (a.tenants) {
                deleteOk = false
            }
        }
        def tenants = crmSecurityService.getTenants(crmUser.username)
        def role
        for (r in ['admin', 'user']) {
            if (crmSecurityService.hasRole(r, null, crmUser.username)) {
                role = r
                break
            }
        }

        switch (request.method) {
            case 'GET':
                return [crmUser    : crmUser, tenantList: tenants, role: role,
                        accountList: crmAccountService.getAccounts(crmUser.username), deleteOk: deleteOk]
            case 'POST':
                if (params.version && crmUser.version) {
                    def version = params.version.toLong()
                    if (crmUser.version > version) {
                        crmUser.errors.rejectValue('version', 'crmUser.optimistic.locking.failure',
                                [message(code: 'crmUser.label', default: 'User')] as Object[],
                                "Another user has updated this user while you were editing")
                        render view: 'edit', model: [crmUser    : crmUser, tenantList: tenants,
                                                     accountList: crmAccountService.getAccounts(crmUser.username), deleteOk: deleteOk]
                        return
                    }
                }

                bindData(crmUser, params, [include: CrmUser.BIND_WHITELIST])

                if (!crmUser.save(flush: true)) {
                    render view: 'edit', model: [crmUser    : crmUser, tenantList: tenants,
                                                 accountList: crmAccountService.getAccounts(crmUser.username), deleteOk: deleteOk]
                    return
                }
                if (params.password1) {
                    if (params.password1 == params.password2) {
                        crmSecurityService.updateUser(crmUser, [password: params.password1, status: CrmUser.STATUS_ACTIVE])
                    } else {
                        flash.error = message(code: 'settings.password.not.equal', default: "Passwords were not equal")
                        render view: 'edit', model: [crmUser    : crmUser, tenantList: tenants,
                                                     accountList: crmAccountService.getAccounts(crmUser.username), deleteOk: deleteOk]
                        return
                    }
                }

                if (params.role != role) {
                    crmSecurityService.removeUserRole(crmUser, role)
                    crmSecurityService.addUserRole(crmUser, params.role)
                }

                flash.success = message(code: 'crmUser.updated.message', args: [message(code: 'crmUser.label', default: 'User'), crmUser.toString()])
                redirect action: 'list'
                break
        }
    }

    @Transactional
    def delete() {
        def crmUser = CrmUser.get(params.id)
        if (!crmUser) {
            flash.error = message(code: 'crmUser.not.found.message', args: [message(code: 'crmUser.label', default: 'User'), params.id])
            redirect action: 'list'
            return
        }
        def deleteOk = true
        def accounts = crmAccountService.getAccounts(crmUser.username)
        for (a in accounts) {
            if (a.tenants) {
                deleteOk = false
            }
        }

        if (!deleteOk) {
            flash.error = message(code: 'crmUser.delete.accounts.message', args: [message(code: 'crmUser.label', default: 'User'), crmUser.toString()])
            redirect action: 'edit', id: crmUser.id
            return
        }

        def tombstone = crmUser.toString()
        try {
            CrmUser.withTransaction { tx ->
                for (a in accounts) {
                    crmAccountService.deleteAccount(a)
                }
                crmSecurityService.deleteUser(crmUser.username)
            }
            flash.warning = message(code: 'crmUser.deleted.message', args: [message(code: 'crmUser.label', default: 'User'), tombstone])
            redirect action: 'list'
            return
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete user [${crmUser.username}]", e)
        }
        flash.error = message(code: 'crmUser.not.deleted.message', args: [message(code: 'crmUser.label', default: 'User'), tombstone])
        redirect action: 'edit', id: params.id
    }
}
