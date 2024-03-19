// ignore_for_file: constant_identifier_names

enum FormStatus {
  started,
  finished,
  not_started,
  waiting,
  error;  // error is just for the frontend

  static FormStatus fromString(String str) {
    for (var value in FormStatus.values) {
      if (value.name.toLowerCase() == str.toLowerCase()) return value;
    }
    throw ArgumentError('Invalid form type: $str');
  }
}
