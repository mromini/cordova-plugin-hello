/* **
* parte client
*/

var Plugin = Plugin || {};

Plugin.Hello = {};

/*
 * Init plugin
 */
Plugin.Hello.init = function ()
{
  //
};

Plugin.Hello.greet = function (req)
{
	var successCb = function (result) 
	{
		req.setResult(result);
	};
	var errorCb = function () 
	{
		req.setError("Aiuto, errore");
	};
	var msg = req.params.message;
	//
	hello.greet(msg, successCb, errorCb);
};

Plugin.Hello.encrypt = function (req)
{
	var successCb = function (result) 
	{
		req.setResult(result);
	};
	var errorCb = function () 
	{
		req.setError("Aiuto, errore");
	};
	var email = req.params.email;
    var password = req.params.password;
	//
	hello.encrypt(email, password, successCb, errorCb);
}