#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'xf_speech_plugin'
  s.version          = '0.0.1'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'

  s.frameworks = 'AVFoundation', 'SystemConfiguration', 'Foundation', 'CoreTelephony', 'AudioToolbox', 'UIKit', 'CoreLocation', 'QuartzCore', 'CoreGraphics', 'CoreTelephony', 'CoreTelephony', 'CoreTelephony', 'CoreTelephony'
  s.libraries = 'c++', 'z'
  s.vendored_frameworks = 'Frameworks/iflyMSC.framework'

  s.ios.deployment_target = '8.0'
end


