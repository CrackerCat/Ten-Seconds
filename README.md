# Ten-Seconds

[![Build Status](https://travis-ci.org/Gh0u1L5/Ten-Seconds.svg?branch=master)](https://travis-ci.org/Gh0u1L5/Ten-Seconds)

---

10秒是一个使用Kotlin与C编写的用于生成和管理账号密码的Android应用。对，我知道类似的软件已经有不少了，但是10秒有自己的特色。

云侧目前使用Google Firebase实现，但是由于API层进行了良好的封装，所以可以很轻松地更换为AWS、阿里云或开发者自己编写的后端接口。

## 基本操作
* 用户可以使用自己的Google账号登录应用，登录后可以在不同设备间自动同步数据。
  - 因此，黑客想要攻击你的话，首先先要攻破Google账号的安全防护。
  - 其实本应用的核心功能离线状态下也能用，但是因为懒所以还没做离线情况的设计。
* 点击右下角的浮动按钮可以添加新的身份（比如“生活”、“工作”），每个身份都需要设定一个独立的访问口令，以保证一个身份被攻破之后不会影响到其他身份的安全。
  - 口令会被做成一个密钥，锁在本地TrustZone里，与系统电路 __保持物理隔离__ 。
  - 每次访问密钥都需要用户 __指纹验证__ ，即使你的手机系统完全被黑客掌控，对方仍然需要当面把你的手指按在传感器上才能访问你的密钥。
* 每个身份可以添加若干个账户，账户的格式以类似邮箱的格式组织（如 abc@gmail.com)。
  - 在添加账户时，可以指定想要的密码长度和密码中用到的字符种类（纯数字、小写字母、大写字母等）。
* 添加后长按账户，会弹出指纹认证的提示，认证通过后，会使用密钥和账户信息计算出一个复杂密码。
  - 这个密码只会在内存里存活 __10秒__ ，10秒后它的一切痕迹都会被彻底抹除干净。
  - 用户可以长按任意一个文本框，激活名为“安定区”的服务，将密码安全地粘贴到文本框里。

## 项目特色
* 同类项目大多使用系统自带的PBKDF2算法生成密钥，而10秒采用了 __scrypt__ 算法
  - scrypt算法是目前公认的能够更好对抗GPU/ASIC并行破解的KDF算法，更多细节可以参见[Wikipedia](https://en.wikipedia.org/wiki/Scrypt)和[这篇回答](https://crypto.stackexchange.com/questions/8159/what-is-the-difference-between-scrypt-and-pbkdf2)。
  - 为安全实现scrypt算法，本项目将目前最新版的 OpenSSL 1.1.1-pre8 和 Tarsnap官方版的 scrypt 1.2.1 移植到了Android平台，并封装了一个[JNI接口](https://github.com/Gh0u1L5/Ten-Seconds/blob/master/app/src/main/cpp/crypto-engine.c)。
* 同类项目大多使用Autofill框架，而10秒采用了Accessibility框架
  - Autofill框架只能将密码填写到特定应用的特定表单中，而Accessiblity框架给予用户更高的自由度，允许用户通过长按的方式将密码粘贴到任意文本框中。
  - Accessibility框架同时也保证了密码的安全性。不同于直接使用系统的粘贴板，Accessibility框架保证了密码被锁死在应用内存中，其他应用无法随意访问。
