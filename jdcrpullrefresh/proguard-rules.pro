# 在这里添加项目专用的 ProGuard 规则。
# 可通过 build.gradle 中的 proguardFiles 配置控制应用哪些规则文件。
#
# 更多信息请参阅：
#   http://developer.android.com/guide/developing/tools/proguard.html

# 如果项目通过 WebView 使用 JavaScript，请取消下面规则的注释，
# 并填写 JavaScript 接口类的完整限定名：
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# 如需保留用于调试堆栈的行号信息，请取消下面规则的注释。
#-keepattributes SourceFile,LineNumberTable

# 如果保留了行号信息，可取消下面规则的注释以隐藏原始源文件名。
#-renamesourcefileattribute SourceFile
