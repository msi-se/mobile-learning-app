// ignore_for_file: constant_identifier_names

enum QuestionType {
  slider,
  stars,
  fulltext,
  yes_no,
  single_choice,
  multiple_choice,
  word_cloud;

  static QuestionType fromString(String str) {
    for (var value in QuestionType.values) {
      if (value.name.toLowerCase() == str.toLowerCase()) return value;
    }
    throw ArgumentError('Invalid form type: $str');
  }
}
