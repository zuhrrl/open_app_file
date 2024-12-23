library open_app_file;

export 'src/common/open_result.dart';
export 'src/plaform/open_app_file.dart'
    if (dart.library.html) 'src/web/open_app_file.dart';
