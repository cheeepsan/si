
class Message {
    constructor(message, user, dataObjectType, data) {
        this.message = message;
        this.user = user;
        this.dataObjectType = dataObjectType;
        this.data = data;
    }

}

class User {
    constructor(id, name) {
        this.id = id;
        this.name = name;
    }

}


class SiText {
    constructor(text) {
        this.text = text;
    }
}

