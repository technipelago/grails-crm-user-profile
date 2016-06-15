<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmUser.label', default: 'User')}"/>
    <title><g:message code="crmUser.list.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmUser.list.title" subtitle="Sökningen resulterade i ${totalCount} st användare"
            args="[entityName]">
</crm:header>

<table class="table table-striped">
    <thead>
    <tr>
        <crm:sortableColumn property="username"
                            title="${message(code: 'crmUser.username.label', default: 'Username')}"/>

        <crm:sortableColumn property="name"
                            title="${message(code: 'crmUser.name.label', default: 'Name')}"/>

        <crm:sortableColumn property="company"
                            title="${message(code: 'crmUser.company.label', default: 'Company')}"/>

        <crm:sortableColumn property="email"
                            title="${message(code: 'crmUser.email.label', default: 'Email')}"/>

        <crm:sortableColumn property="telephone"
                            title="${message(code: 'crmUser.telephone.label', default: 'Telephone')}"/>
        <th>Roll</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" var="crmUser">
        <tr class="${crmUser.enabled ? '' : 'disabled'}">

            <td>
                <g:link action="edit" id="${crmUser.id}">
                    ${fieldValue(bean: crmUser, field: "username")}
                </g:link>
            </td>

            <td>${fieldValue(bean: crmUser, field: "name")}</td>
            <td>${fieldValue(bean: crmUser, field: "company")}</td>
            <td>${fieldValue(bean: crmUser, field: "email")}</td>
            <td>${fieldValue(bean: crmUser, field: "telephone")}</td>
            <td><crm:userRoles username="${crmUser.username}" var="r">${r.role}</crm:userRoles></td>
        </tr>
    </g:each>
    </tbody>
</table>

<crm:paginate total="${totalCount}"/>

<div class="form-actions btn-toolbar">
    <crm:button type="link" action="create" visual="success" icon="icon-user icon-white" label="crmUser.button.create.label"/>
</div>

</body>
</html>
