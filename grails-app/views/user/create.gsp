<%@ page import="grails.plugins.crm.security.CrmUser" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmUser.label', default: 'User')}"/>
    <title><g:message code="crmUser.create.title" args="[entityName, crmUser]"/></title>
</head>

<body>

<crm:header title="crmUser.create.title" subtitle="${crmUser.username}" args="[entityName, crmUser.name]"/>

<g:hasErrors bean="${crmUser}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmUser}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="create">
    <f:with bean="crmUser">

        <div class="row-fluid">

            <div class="span6">
                <div class="control-group ">
                    <label class="control-label" for="username"><g:message code="crmUser.username.label"
                                                                           default="User Name"/></label>

                    <div class="controls">
                        <g:textField name="username" value="${crmUser.username}" autofocus=""/>
                    </div>
                </div>
                <f:with bean="crmUser">
                    <f:field property="email"/>
                    <f:field property="name"/>
                    <f:field property="company"/>
                    <f:field property="telephone"/>
                </f:with>
            </div>

            <div class="span6">
                <div class="control-group">
                    <label class="control-label">
                        <g:message code="crmRole.label" default="Role"/>
                    </label>
                    <div class="controls">
                        <g:select name="role" from="${roles}" value="${role}" optionKey="value" optionValue="label"/>
                    </div>
                </div>

                <div class="control-group ">
                    <label class="control-label" for="password1">
                        <g:message code="crmSettings.password1.label" default="Password"/>
                    </label>

                    <div class="controls">
                        <g:passwordField name="password1" value="" class="span6"/>
                    </div>
                </div>

                <div class="control-group ">
                    <label class="control-label" for="password2">
                        <g:message code="crmSettings.password2.label" default="Repeat Password"/>
                    </label>

                    <div class="controls">
                        <g:passwordField name="password2" value="" class="span6"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="form-actions">
            <crm:button visual="success" icon="icon-ok icon-white" label="crmUser.button.save.label"/>
        </div>

    </f:with>
</g:form>

</body>
</html>
