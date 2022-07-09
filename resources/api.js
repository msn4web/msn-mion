function __getXmlHttpRequest() {
    var xmlhttp;
    try {
        xmlhttp = new ActiveXObject("MSXML2.XMLHTTP");
    } catch (exception1) {
        try {
            xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        } catch (exception2) {
            xmlhttp = false;
        }
    }

    if (!xmlhttp && window.XMLHttpRequest) {
        xmlhttp = new XMLHttpRequest();
    }

    return xmlhttp;
}

window.MsnClientState = {
    NONE: 0,
    READY: 1,
    LOGGING_IN: 2,
    LOGIN_FAILED: 3,
    DEAD: 4
}

window.MsnStatus = {
    NLN: "online",
    BRB: "brb",
    AWY: "away",
    BSY: "busy",
    LUN: "lunch",
    PHN: "phone",
    IDL: "idle",
    OFF: "offline",
    HID: "offline"
}

class MsnApiException extends Error {
    constructor(message) {
        super(message);
        this.name = "MsnApiException";
    }
}

class MsnClient {
    constructor(prefix = "/") {
        this.prefix = prefix;
        this.state  = MsnClientState.NONE;
        this.key    = "";
        this.pw     = [];
        this.fix    = 0;

        this.listeners = {};
    }

    _emitEvent(type, data) {
        if(typeof this.listeners[type] === "undefined") {
            if(type === MsnClient.MULTICAST_EVENT_TYPE) {
                return;
            } else {
                data["@type"] = type;
                return this._emitEvent(MsnClient.MULTICAST_EVENT_TYPE, data);
            }
        }

        this.listeners[type].forEach(delegate => delegate(data));

        if(type !== MsnClient.MULTICAST_EVENT_TYPE) {
            data["@type"] = type;
            this._emitEvent(MsnClient.MULTICAST_EVENT_TYPE, data);
        }
    }

    addEventListener(type, listener) {
        if(typeof this.listeners[type] === "undefined")
            this.listeners[type] = [];

        this.listeners[type].push(listener);
    }

    request(method, params = {}, noMethodPrefix = false) {
        return new Promise((resolve, reject) => {
            let xhr = __getXmlHttpRequest();
            xhr.open("POST", this.prefix + (noMethodPrefix ? "" : "method/") + method, true);
            xhr.setRequestHeader("Content-Type", "application/json");
            if(this.state === MsnClientState.READY || this.state === MsnClientState.LOGGING_IN)
                xhr.setRequestHeader("Authorization", "Basic " + this.key);

            xhr.onreadystatechange = () => {
                if(xhr.readyState === 4) {
                    if(xhr.status >= 200 && xhr.status < 400) {
                        resolve(JSON.parse(xhr.responseText));
                    } else {
                        try {
                            reject(JSON.parse(xhr.responseText));
                        } catch(e) {
                            console.error(e);
                            reject(e);
                        }
                    }
                }
            };

            xhr.send(JSON.stringify(params));
        });
    }

    async _longPooler() {
        try {
            let events = await this.request("getUpdates");
            events.updates.forEach(update => this._emitEvent(update["@type"].toLowerCase(), update.data));
        } catch(e) {}

        if(this.state === MsnClientState.READY)
            this._longPooler();
    }

    _postLoginInit() {
        this._longPooler();

        setInterval(() => {
            this.request("noOp");
        }, 60 * 1000);
    }

    async login(username, password) {
        if(this.state !== MsnClientState.NONE && this.state !== MsnClientState.LOGIN_FAILED && this.state !== MsnClientState.DEAD)
            throw new MsnApiException("Already logged in");

        let auth = await this.request("acquireSession", {
            email: username,
            password: password
        }, true);

        this.state = MsnClientState.LOGGING_IN;
        this.key   = btoa(username + ":" + auth.key);

        let events = await this.request("getUpdates");
        events.updates.forEach(update => {
            if(update["@type"] === "UNAUTHORIZED") {
                this.state = MsnClientState.LOGIN_FAILED;
                this._emitEvent("loginFailed", {});
                throw new MsnApiException("Invalid username/password");
            } else if(update["@type"] === "READY") {
                this.state = MsnClientState.READY;
                this._emitEvent("clientReady", {});
                console.debug("Logged in as ", username);
            }
        });

        if(this.state === MsnClientState.LOGGING_IN) {
            console.error("Could not receive auth confirmation within a reasonable amount of time.");
            this.state = MsnClientState.LOGIN_FAILED;
            throw new MsnApiException("Gateway timeout");
        }

        this.pw = [username, password];
        this._postLoginInit();
    }

    async logout() {
        await this.request("logOut");
        this.state = MsnClientState.NONE;
    }

    async getMe() {
        return await this.request("getMe");
    }

    async sendMessage(email, text) {
        return await this.request("sendMessage", {
            peer: email,
            text: text
        });
    }

    async getContacts() {
        return await this.request("contacts", {});
    }

    async getContact(email) {
        return await this.request("contact", {
            act: "fetch",
            email: email
        });
    }

    async addContact(email, name) {
        return await this.request("contact", {
            act: "add",
            email: email,
            name: name
        });
    }

    async removeContact(email, block = true) {
        return await this.request("contact", {
            act: "remove",
            email: email,
            block: block
        });
    }

    async setPresence(status) {
        if(status === window.MsnStatus.OFF)
            console.warn("Will mark client as offline by hiding presence");

        return await this.request("presence", {
            status: status
        });
    }
}

MsnClient.MULTICAST_EVENT_TYPE = "**";