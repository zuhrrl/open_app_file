class OpenResult {
  ResultType type;
  String message;

  OpenResult(this.type, {this.message = "done"});

  OpenResult.fromJson(Map<String, dynamic> json)
      : type = _typeFromCode(json['type']),
        message = json['message'];

  static ResultType _typeFromCode(int? typeCode) {
    switch (typeCode) {
      case -1:
        return ResultType.noAppToOpen;
      case -2:
        return ResultType.fileNotFound;
      case -3:
        return ResultType.permissionDenied;
      case -4:
        return ResultType.error;
    }
    return ResultType.done;
  }
}

enum ResultType {
  done,
  fileNotFound,
  noAppToOpen,
  permissionDenied,
  error,
}
