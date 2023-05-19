
Pod::Spec.new do |s|
  s.name         = "RNTrustVisionRnsdkFramework"
  s.version      = "2.1.9-Tiki"
  s.summary      = "RNTrustVisionRnsdkFramework"
  s.description  = <<-DESC
                  RNTrustVisionRnsdkFramework
                   DESC
  s.homepage     = "https://github.com/tsocial/TrustVisionRNSDKFramework"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/author/RNTrustVisionRnsdkFramework.git", :tag => "release" }
#  s.source_files  = "RNTrustVisionRnsdkFramework/**/*.{h,m}"
  s.source_files  = "ios/**/*.{h,m}"
  s.requires_arc = true

  s.resources = [
    'ios/Frameworks/TrustVisionSDK.bundle',
    'ios/Frameworks/TrustVisionCoreSDK.bundle'
  ]
  s.vendored_frameworks = [
    'ios/Frameworks/TrustVisionSDK.xcframework',
    'ios/Frameworks/TrustVisionCoreSDK.xcframework',
    'ios/Frameworks/TrustVisionAPI.xcframework'
  ]

  s.dependency "React"
  s.dependency 'TensorFlowLiteSwift', '~> 2.6.0'
  s.dependency 'PromiseKit', '~> 6.8'
  s.dependency 'CocoaLumberjack/Swift'
  s.dependency 'lottie-ios'
  
end
