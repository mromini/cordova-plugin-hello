#import "HWPHello.h"
#import <Cordova/CDV.h>

@implementation HWPHello

- (void)greet:(CDVInvokedUrlCommand*)command
{

    NSString* name = [[command arguments] objectAtIndex:0];
    NSString* msg = [NSString stringWithFormat: @"Hello, %@", name];

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)encrypt:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* email = [command.arguments objectAtIndex:0];
    NSString* pwd = [command.arguments objectAtIndex:1];
    
    NSMutableDictionary *par =  [NSMutableDictionary dictionary];
    [par setValue:email forKey:@"EMAIL"];
    [par setValue:pwd forKey:@"PWD"];
    
    NSData* result = [self TSID_API_REQ:par];
    NSString *receivedDataString = [[NSString alloc] initWithData:result encoding:NSUTF8StringEncoding];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:receivedDataString];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (NSData *)decodeAndPrintCipherBase64Data:(NSString *)cipherText
                               usingHexKey:(NSString *)hexKey
                                     hexIV:(NSString *)hexIV
{
    NSData *data = [[NSData alloc] initWithBase64EncodedString:cipherText options:0];
    NSAssert(data != nil, @"Couldn't base64 decode cipher text");
    
    NSData *decryptedPayload = [data originalDataWithHexKey:hexKey
                                                      hexIV:hexIV];
    
    if (decryptedPayload) {
        NSString *plainText = [[NSString alloc] initWithData:decryptedPayload encoding:NSUTF8StringEncoding];
        //NSLog(@"Decrypted Result: %@", plainText);
    }
    
    return decryptedPayload;
}

- (NSString *)encodeAndPrintPlainText:(NSString *)plainText
                          usingHexKey:(NSString *)hexKey
                                hexIV:(NSString *)hexIV
{
    NSString *result=@"";
    NSData *data = [plainText dataUsingEncoding:NSUTF8StringEncoding];
    
    NSData *encryptedPayload = [data encryptedDataWithHexKey:hexKey
                                                       hexIV:hexIV];
    
    if (encryptedPayload) {
        NSString *cipherText = [encryptedPayload base64EncodedStringWithOptions:0];
        result=cipherText;
        //NSLog(@"Encryped Result: %@", cipherText);
    }
    
    return result;
}
-(NSData *)TSID_API_REQ:(NSMutableDictionary *)par
{
    NSData *risultato = [[NSString stringWithFormat:@"KO"] dataUsingEncoding:NSUTF8StringEncoding];
    //
    NSString *AppCode = @"EXTERNAL\\SALESAPP";
    NSString *Email = [par objectForKey:@"EMAIL"];
    NSString *Password = [par objectForKey:@"PWD"];
    NSTimeInterval time = ([[NSDate date] timeIntervalSince1970]); // returned as a double
    NSString *digits =[NSString stringWithFormat:@"%ld",(long)time]; // this is the first 10 digits
    NSString *decimalDigits =[NSString stringWithFormat:@"%d", (int)(fmod(time, 1) * 1000)]; // this will get the 3 missing digits
    if ([decimalDigits length]==2)
        decimalDigits= [NSString stringWithFormat:@"%@%@", @"0",decimalDigits];
    //
    NSString *ReqDate =[NSString stringWithFormat:@"%@%@",digits ,decimalDigits];
    NSString *AppPwd =@"6bT4W6zxNUhuqXzZuErM88NUd8YM4jFw";
    NSString *MAC = [NSString stringWithFormat:@"%@%@%@%@%@", AppCode, Email, Password,ReqDate,AppPwd];
    NSString *MAC_MD5 = [MAC MD5].uppercaseString;
    
    NSDictionary *dict = @{ @"AppCode" : AppCode,
                            @"Email" : Email,
                            @"Password" : Password,
                            @"RequestDate" : ReqDate,
                            @"MAC": MAC_MD5
                            };
    // Dictionary convertable to JSON ?
    if ([NSJSONSerialization isValidJSONObject:dict])
    {
        NSError *error = nil;
        NSData *json;
        NSString *toCrypt = @"";
        
        // Serialize the dictionary
        json = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
        
        // If no errors, let's view the JSON
        if (json != nil && error == nil) {
            toCrypt = [[NSString alloc] initWithData:json encoding:NSUTF8StringEncoding];
            NSString *hexKey = @"565757646A5848435638423347574455417A3779764B35504351477652784D54";
            NSString *hexIV = @"6A3363356B4256615845523371367A58";
            //
            NSString *cipherText = [self encodeAndPrintPlainText:toCrypt usingHexKey:hexKey hexIV:hexIV];
            //
            risultato = [cipherText dataUsingEncoding:NSUTF8StringEncoding];
        }
    }
    //
    return risultato;
}

@end
