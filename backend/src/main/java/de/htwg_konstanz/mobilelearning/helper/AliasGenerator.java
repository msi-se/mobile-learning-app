package de.htwg_konstanz.mobilelearning.helper;

/**
 * static class to generate random aliases
like curios zebra, intelligent tiger, strange squirrel, ... (positive adjectives)
*/
public class AliasGenerator {

    private static String[] adjectives = {
            "curious", "intelligent", "strange", "funny", "happy", "crazy", "silly", "friendly", "clever", "brave",
            "kind", "creative", "adventurous", "energetic", "charming", "enthusiastic", "playful", "vibrant",
            "cheerful", "graceful", "lively", "optimistic", "eloquent", "dynamic", "innovative", "eclectic", "fearless",
            "magnetic", "sparkling", "harmonious", "outstanding", "brilliant", "agreeable", "amiable", "appealing",
            "astute", "authentic", "bighearted", "bright", "comfortable", "content", "cultured", "cute", "dashing",
            "defiant", "delightful", "discerning", "educated", "experienced", "fabulous", "fanciful", "farsighted",
            "fashionable", "generous", "gentle", "gifted", "hopeful", "humble", "illustrious", "incisive", "joyful",
            "knowledgeable", "likable", "lucky", "mature", "popular", "professional", "punctual", "qualified",
            "resilient", "resourceful", "smart", "steady", "strong", "talented", "teachable", "unaffected",
            "well-respected", "wise", "amicable", "amusing", "anxious", "broad-minded", "carefree", "considerate",
            "consistent", "cowardly", "cynical", "dedicated", "deep", "dependable", "determined", "diligent", "direct",
            "dispassionate", "earnest", "easygoing", "egotistical", "emotional", "empathetic", "exacting",
            "fair-minded", "faithful", "giving", "hilarious", "honest", "humorous", "independent", "inflexible",
            "intellectual", "moody", "narrow-minded", "nervous", "nice", "open-minded", "overemotional", "passionate",
            "pessimistic", "powerful", "practical", "proud", "reliable", "self-centered", "self-confident", "sensible",
            "sensitive", "shrewd", "sincere", "sympathetic", "thoughtful", "thoughtless", "tough", "trustworthy",
            "unassuming", "understanding", "untrustworthy", "upbeat", "vain", "warmhearted", "wise", "affectionate",
            "aggressive", "aloof", "ambitious", "apologetic", "arrogant", "artistic", "assertive", "aware", "boastful",
            "bold", "boring", "bossy", "calm", "careful", "careless", "cautious", "chatty", "chivalrous", "civil",
            "clingy", "collaborative", "commanding", "communicative", "compassionate", "competitive", "compliant",
            "compromising", "confrontational", "conscientious", "cooperative", "courageous", "courteous", "cruel",
            "deceitful", "decisive", "defensive", "deliberate", "devious", "dexterous", "diplomatic", "discreet",
            "dishonest", "domineering", "efficient", "enterprising", "entrepreneurial", "envious", "extravagant",
            "extroverted", "fastidious", "ferocious", "flamboyant", "flirtatious", "forceful", "frugal", "gallant",
            "grumpy", "hardworking", "helpful", "hostile", "idle", "imaginative", "impartial", "impatient", "impolite",
            "impulsive", "inconsiderate", "indecisive", "indiscreet", "industrious", "intolerant", "introverted",
            "inventive", "irresponsible", "jealous", "laid-back", "lazy", "loud", "loving", "loyal", "mean", "modest",
            "nasty", "neat", "non-judgmental", "observant", "obstinate", "organized", "overcritical", "patient",
            "patronizing", "persistent", "polite", "possessive", "proactive", "quick-tempered", "quiet", "rational",
            "reserved", "responsive", "romantic", "rude", "secretive", "self-disciplined", "selfish", "shy", "sociable",
            "straightforward", "stubborn", "tactless", "talkative", "tidy", "unpredictable", "unreliable", "untidy",
            "vague", "wild", "witty", };

    private static String[] animals = {
            "aardvark", "albatross", "alligator", "alpaca", "ant", "anteater", "antelope", "ape", "armadillo", "donkey",
            "baboon", "badger", "barracuda", "bat", "bear", "beaver", "bee", "bison", "boar", "buffalo", "butterfly",
            "camel", "capybara", "caribou", "cassowary", "cat", "caterpillar", "cattle", "chamois", "cheetah",
            "chicken", "chimpanzee", "chinchilla", "chough", "clam", "cobra", "cockroach", "cod", "cormorant", "coyote",
            "crab", "crane", "crocodile", "crow", "curlew", "deer", "dinosaur", "dog", "dogfish", "dolphin", "dotterel",
            "dove", "dragonfly", "duck", "dugong", "dunlin", "eagle", "echidna", "eel", "eland", "elephant", "elk",
            "emu", "falcon", "ferret", "finch", "fish", "flamingo", "fly", "fox", "frog", "gaur", "gazelle", "gerbil",
            "giraffe", "gnat", "gnu", "goat", "goldfinch", "goldfish", "goose", "gorilla", "goshawk", "grasshopper",
            "grouse", "guanaco", "gull", "hamster", "hare", "hawk", "hedgehog", "heron", "herring", "hippopotamus",
            "hornet", "horse", "human", "hummingbird", "hyena", "ibex", "ibis", "jackal", "jaguar", "jay", "jellyfish",
            "kangaroo", "kingfisher", "koala", "kookabura", "kouprey", "kudu", "lapwing", "lark", "lemur", "leopard",
            "lion", "llama", "lobster", "locust", "loris", "louse", "lyrebird", "magpie", "mallard", "manatee",
            "mandrill", "mantis", "marten", "meerkat", "mink", "mole", "mongoose", "monkey", "moose", "mosquito",
            "mouse", "mule", "narwhal", "newt", "nightingale", "octopus", "okapi", "opossum", "oryx", "ostrich",
            "otter", "owl", "oyster", "panther", "parrot", "partridge", "peafowl", "pelican", "penguin", "pheasant",
            "pig", "pigeon", "pony", "porcupine", "porpoise", "quail", "quelea", "quetzal", "rabbit", "raccoon", "rail",
            "ram", "rat", "raven", "red deer", "red panda", "reindeer", "rhinoceros", "rook", "salamander", "salmon",
            "sand dollar", "sandpiper", "sardine", "scorpion", "seahorse", "seal", "shark", "sheep", "shrew", "skunk",
            "snail", "snake", "sparrow", "spider", "spoonbill", "squid", "squirrel", "starling", "stingray", "stinkbug",
            "stork", "swallow", "swan", "tapir", "tarsier", "termite", "tiger", "toad", "trout", "turkey", "turtle",
            "viper", "vulture", "wallaby", "walrus", "wasp", "weasel", "whale", "wildcat", "wolf", "wolverine",
            "wombat", "woodcock", "woodpecker", "worm", "wren", "yak", "zebra", };

    /**
     * generates a random alias from adjectives & animals.
     * @return a random alias
     */
    public static String generateAlias() {
        int adjectiveIndex = (int) (Math.random() * adjectives.length);
        int animalIndex = (int) (Math.random() * animals.length);

        // convert the first letter to uppercase
        String adjective = adjectives[adjectiveIndex];
        adjective = adjective.substring(0, 1).toUpperCase() + adjective.substring(1);
        String animal = animals[animalIndex];
        animal = animal.substring(0, 1).toUpperCase() + animal.substring(1);
        return adjective + " " + animal;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(generateAlias());
        }
    }

}
