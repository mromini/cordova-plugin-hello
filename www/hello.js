/*global cordova, module*/

module.exports = {
    greet: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Hello", "greet", [name]);
    },
    encrypt: function (email, password, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Hello", "encrypt", [email, password]);
    }
};
