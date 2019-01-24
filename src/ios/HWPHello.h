#import <Cordova/CDV.h>
#import <CommonCrypto/CommonDigest.h>
#import <CommonCrypto/CommonCryptor.h>

@interface HWPHello : CDVPlugin

- (void) greet:(CDVInvokedUrlCommand*)command;
- (void)encrypt:(CDVInvokedUrlCommand*)command;

@end

@interface NSData (AES256Encryption)
- (NSData *)encryptedDataWithHexKey:(NSString*)hexKey hexIV:(NSString *)hexIV;
- (NSData *)originalDataWithHexKey:(NSString*)hexKey hexIV:(NSString *)hexIV;
@end

@implementation NSData (AES256Encryption)

+ (NSData *)dataFromHexString:(NSString *)string
{
    string = string.lowercaseString;
    NSMutableData *data = [[NSMutableData alloc] initWithCapacity:string.length/2];
    unsigned char whole_byte;
    char byte_chars[3] = {'\0','\0','\0'};
    int i = 0;
    NSUInteger length = string.length;
    while (i < length-1) {
        char c = [string characterAtIndex:i++];
        if (c < '0' || (c > '9' && c < 'a') || c > 'f')
            continue;
        byte_chars[0] = c;
        byte_chars[1] = [string characterAtIndex:i++];
        whole_byte = strtol(byte_chars, NULL, 16);
        [data appendBytes:&whole_byte length:1];
    }
    return data;
}

+ (void)fillDataArray:(char **)dataPtr length:(NSUInteger)length usingHexString:(NSString *)hexString
{
    NSData *data = [NSData dataFromHexString:hexString];
    NSAssert((data.length + 1) == length, @"The hex provided didn't decode to match length");
    
    unsigned long total_bytes = (length + 1) * sizeof(char);
    
    *dataPtr = malloc(total_bytes);
    bzero(*dataPtr, total_bytes);
    memcpy(*dataPtr, data.bytes, data.length);
}

- (NSData *)encryptedDataWithHexKey:(NSString*)hexKey hexIV:(NSString *)hexIV
{
    // Fetch key data and put into C string array padded with \0
    char *keyPtr;
    [NSData fillDataArray:&keyPtr length:kCCKeySizeAES256+1 usingHexString:hexKey];
    
    // Fetch iv data and put into C string array padded with \0
    char *ivPtr;
    [NSData fillDataArray:&ivPtr length:kCCKeySizeAES128+1 usingHexString:hexIV];
    
    // For block ciphers, the output size will always be less than or equal to the input size plus the size of one block because we add padding.
    // That's why we need to add the size of one block here
    NSUInteger dataLength = self.length;
    size_t bufferSize = dataLength + kCCBlockSizeAES128;
    void *buffer = malloc( bufferSize );
    
    size_t numBytesEncrypted = 0;
    CCCryptorStatus cryptStatus = CCCrypt(kCCEncrypt, kCCAlgorithmAES, kCCOptionPKCS7Padding,
                                          keyPtr, kCCKeySizeAES256,
                                          ivPtr /* initialization vector */,
                                          [self bytes], [self length], /* input */
                                          buffer, bufferSize, /* output */
                                          &numBytesEncrypted);
    free(keyPtr);
    free(ivPtr);
    
    if(cryptStatus == kCCSuccess) {
        return [NSData dataWithBytesNoCopy:buffer length:numBytesEncrypted];
    }
    
    free(buffer);
    return nil;
}

- (NSData *)originalDataWithHexKey:(NSString*)hexKey hexIV:(NSString *)hexIV
{
    // Fetch key data and put into C string array padded with \0
    char *keyPtr;
    [NSData fillDataArray:&keyPtr length:kCCKeySizeAES256+1 usingHexString:hexKey];
    
    // Fetch iv data and put into C string array padded with \0
    char *ivPtr;
    [NSData fillDataArray:&ivPtr length:kCCKeySizeAES128+1 usingHexString:hexIV];
    
    // For block ciphers, the output size will always be less than or equal to the input size plus the size of one block because we add padding.
    // That's why we need to add the size of one block here
    NSUInteger dataLength = self.length;
    size_t bufferSize = dataLength + kCCBlockSizeAES128;
    void *buffer = malloc( bufferSize );
    
    size_t numBytesDecrypted = 0;
    CCCryptorStatus cryptStatus = CCCrypt( kCCDecrypt, kCCAlgorithmAES, kCCOptionPKCS7Padding,
                                          keyPtr, kCCKeySizeAES256,
                                          ivPtr,
                                          [self bytes], dataLength, /* input */
                                          buffer, bufferSize, /* output */
                                          &numBytesDecrypted );
    free(keyPtr);
    free(ivPtr);
    
    if( cryptStatus == kCCSuccess )
    {
        // The returned NSData takes ownership of the buffer and will free it on deallocation
        return [NSData dataWithBytesNoCopy:buffer length:numBytesDecrypted];
    }
    
    free(buffer);
    return nil;
}
@end

@interface NSString (MD5)
- (NSString *)MD5;
@end

@implementation NSString (MD5)
- (NSString*)MD5
{
    // Create pointer to the string as UTF8
    const char *ptr = [self UTF8String];
    
    // Create byte array of unsigned chars
    unsigned char md5Buffer[CC_MD5_DIGEST_LENGTH];
    
    // Create 16 byte MD5 hash value, store in buffer
    CC_MD5(ptr, strlen(ptr), md5Buffer);
    
    // Convert MD5 value in the buffer to NSString of hex values
    NSMutableString *output = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for(int i = 0; i < CC_MD5_DIGEST_LENGTH; i++)
        [output appendFormat:@"%02x",md5Buffer[i]];
    
    return output;
}
@end
