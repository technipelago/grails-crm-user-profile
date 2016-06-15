<%@ page import="grails.plugins.crm.security.CrmTenant; grails.plugins.crm.core.TenantUtils" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="settings.index.title" args="[cmd.name]" default="Settings"/></title>
</head>

<body>

<crm:header title="settings.index.title" subtitle="${cmd.name.encodeAsHTML()}" args="[cmd.name]"/>

<g:hasErrors bean="${cmd}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${cmd}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="profile">

    <div class="tabbable">

        <ul class="nav nav-tabs">
            <li class="active"><a href="#user" data-toggle="tab"><g:message code="settings.tab.user.label"/></a></li>
            <crm:pluginViews location="tabs" var="view">
                <crm:pluginTab id="${view.id}" label="${view.label}" count="${view.model?.totalCount}"/>
            </crm:pluginViews>
        </ul>

        <div class="tab-content">

            <div class="tab-pane active" id="user">

                <div class="row-fluid">
                    <div class="span4">
                        <div class="row-fluid">

                            <f:with bean="${cmd}">
                                <f:field property="username" label="crmUser.username.label">
                                    <g:textField name="username-disabled" disabled="" value="${cmd.username}"
                                                 class="span10"/>
                                    <g:hiddenField name="username" value="${cmd.username}"/>
                                </f:field>
                                <f:field property="name" label="crmUser.name.label" autofocus="" input-class="span10"/>
                                <f:field property="company" label="crmUser.company.label" autofocus=""
                                         input-class="span10"/>
                                <f:field property="email" label="crmUser.email.label" input-class="span10"/>
                                <f:field property="telephone" label="crmUser.telephone.label" input-class="span6"/>
                                <f:field property="postalCode" label="crmUser.postalCode.label" input-class="span6"/>
                            </f:with>
                        </div>
                    </div>

                    <div class="span3">
                        <div class="row-fluid">

                            <f:field bean="${cmd}" property="defaultTenant" label="crmUser.defaultTenant.label">
                                <g:select from="${tenants}" name="defaultTenant" value="${cmd.defaultTenant}"
                                          optionKey="id" noSelection="${['': 'Välj startvy']}"/>
                            </f:field>

                        </div>
                    </div>

                    <div class="span5">
                        <div class="row-fluid">

                            <div class="control-group ">
                                <label class="control-label" for="password1">
                                    <g:message code="crmSettings.password1.label" default="Password"/>
                                </label>

                                <div class="controls">
                                    <g:passwordField name="password1" value="" class="span8"/>
                                </div>
                            </div>

                            <div class="control-group ">
                                <label class="control-label" for="password2">
                                    <g:message code="crmSettings.password2.label" default="Repeat Password"/>
                                </label>

                                <div class="controls">
                                    <g:passwordField name="password2" value="" class="span8"/>
                                </div>
                            </div>

                            <g:if test="${questions}">
                                <h4><g:message code="settings.security.questions.title"
                                               default="Security questions"/></h4>
                                <g:each in="${0..2}" var="n">
                                    <div class="control-group">
                                        <g:select from="${questions}" name="q[${n}]" value="${answers[n]}" class="span6"
                                                  optionValue="${{ message(code: it) }}" noSelection="${['': '']}"/>
                                        <g:textField name="a[${n}]" value="${answers[n] ? '**********' : ''}" class="span5"/>
                                    </div>
                                </g:each>
                            </g:if>
                        </div>
                    </div>
                </div>
            </div>

            <crm:pluginViews location="tabs" var="view">
                <div class="tab-pane tab-${view.id}" id="${view.id}">
                    <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
                </div>
            </crm:pluginViews>

        </div>
    </div>

    <div class="form-actions">
        <crm:button visual="primary" icon="icon-ok icon-white"
                    label="settings.button.update.label"/>
    </div>

</g:form>

</body>
</html>
