// ignore_for_file: constant_identifier_names

enum FormType {
  feedback,
  quiz;

  static FormType fromString(String str) {
    for (var value in FormType.values) {
      if (value.name.toLowerCase() == str.toLowerCase()) return value;
    }
    throw ArgumentError('Invalid form type: $str');
  }
}
