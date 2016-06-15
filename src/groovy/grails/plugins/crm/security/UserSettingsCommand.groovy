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

import grails.validation.Validateable

/**
 * Backing bean for user settings.
 */
@Validateable
class UserSettingsCommand implements Serializable {
    String username
    String name
    String password1
    String password2
    String company
    String email
    String telephone
    String postalCode
    String timezone
    Long defaultTenant

    def crmSecurityService

    Locale locale

    static constraints = {
        importFrom CrmUser, include: ['name', 'company', 'email', 'telephone', 'postalCode']
        username(size: 2..80, maxSize: 80, nullable: false, blank: false)
        timezone(maxSize: 40, nullable: true)
        password1(maxSize: 80, nullable: true, blank: false)
        password2(maxSize: 80, nullable: true, blank: false, validator: { val, obj, errors ->
            if (val || obj.password1) {
                if (val == obj.password1) {
                    def result = obj.crmSecurityService.validatePassword(val, obj.username, obj.locale)
                    for (code in result) {
                        errors.rejectValue('password2', code, code)
                    }
                } else {
                    errors.rejectValue('password2', 'settings.password.not.equal', 'Passwords are not equal')
                }
            }
            return null
        })
        defaultTenant(nullable: true, validator: { val, obj ->
            def rval = null
            if (val) {
                CrmTenant.withNewSession {
                    if (!obj.crmSecurityService.isValidTenant(val)) {
                        rval = ['crmUser.defaultTenant.invalid.message', 'defaultTenant', 'User', val]
                    }
                }
            }
            return rval
        })
    }

    UserSettingsCommand() {}

    void setFromUser(CrmUser user) {
        username = user.username
        name = user.name
        company = user.company
        email = user.email
        telephone = user.telephone
        postalCode = user.postalCode
        timezone = user.timezone
        defaultTenant = user.defaultTenant
    }

    void bindCrmUser(CrmUser user) {

        //user.username = username
        user.name = name
        user.company = company
        user.email = email
        user.telephone = telephone
        user.postalCode = postalCode
        user.timezone = timezone

        if (password1) {
            crmSecurityService.updateUser(user, [password: password1])
        }

        user.defaultTenant = defaultTenant
    }
}
