navigation = {
    settings(global: true) {
        userSettings controller: 'user', action: 'profile', order: 10
    }
    admin(global: true) {
        crmUser controller: 'user', action: 'list', order: 620
    }
}