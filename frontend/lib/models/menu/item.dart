class Item {
  final String preis1;
  final String beilagen;
  final String preis2;
  final String preis3;
  final String einheit;
  final String color;
  final String kennzeichnungen;
  final String description;
  final String language;
  final String category;
  final String title;
  final String icons;

  Item({
    required this.preis1,
    required this.beilagen,
    required this.preis2,
    required this.preis3,
    required this.einheit,
    required this.color,
    required this.kennzeichnungen,
    required this.description,
    required this.language,
    required this.category,
    required this.title,
    required this.icons,
  });

  factory Item.fromJson(Map<String, dynamic> json) {
    return Item(
      preis1: json['preis1'],
      beilagen: json['beilagen'],
      preis2: json['preis2'],
      preis3: json['preis3'],
      einheit: json['einheit'],
      color: json['color'],
      kennzeichnungen: json['kennzeichnungen'],
      description: json['description'],
      language: json['language'],
      category: json['category'],
      title: json['title'],
      icons: json['icons'],
    );
  }
}
