#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'open_app_file'
  s.version          = '3.2.2'
  s.summary          = 'Open app file library.'
  s.description      = <<-DESC
Open app file library.
                       DESC
  s.homepage         = 'https://github.com/yendoplan/open_app_file'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Yendoplan' => 'it@yendoplan.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  
  s.ios.deployment_target = '8.0'
end

