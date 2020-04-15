package com.github.derrop.proxy.protocol;

public interface ProtocolIds {

    interface ClientBound {

        interface Play {
            int KEEP_ALIVE = 0;

            int LOGIN = 1;

            int CHAT = 2;

            int UPDATE_TIME = 3;

            int ENTITY_EQUIPMENT = 4;

            int SPAWN_POSITION = 5;

            int UPDATE_HEALTH = 6;

            int RESPAWN = 7;

            int POSITION = 8;

            int HELD_ITEM_SLOT = 9;

            int BED = 10;

            int ANIMATION = 11;

            int NAMED_ENTITY_SPAWN = 12;

            int COLLECT = 13;

            int SPAWN_ENTITY = 14;

            int SPAWN_ENTITY_LIVING = 15;

            int SPAWN_ENTITY_PAINTING = 16;

            int SPAWN_ENTITY_EXPERIENCE_ORB = 17;

            int ENTITY_VELOCITY = 18;

            int ENTITY_DESTROY = 19;

            int ENTITY = 20;

            int REL_ENTITY_REMOVE = 21;

            int ENTITY_LOOK = 22;

            int REL_ENTITY_MOVE_LOOK = 23;

            int ENTITY_TELEPORT = 24;

            int ENTITY_HEAD_ROTATION = 25;

            int ENTITY_STATUS = 26;

            int ATTACH_ENTITY = 27;

            int ENTITY_METADATA = 28;

            int ENTITY_EFFECT = 29;

            int REMOVE_ENTITY_EFFECT = 30;

            int EXPERIENCE = 31;

            int UPDATE_ATTRIBUTES = 32;

            int MAP_CHUNK = 33;

            int MULTI_BLOCK_CHANGE = 34;

            int BLOCK_CHANGE = 35;

            int BLOCK_ACTION = 36;

            int BLOCK_BREAK_ANIMATION = 37;

            int MAP_CHUNK_BULK = 38;

            int EXPLOSION = 39;

            int WORLD_EVENT = 40;

            int NAMED_SOUND_EFFECT = 41;

            int WORLD_PARTICLES = 42;

            int GAME_STATE_CHANGE = 43;

            int SPAWN_ENTITY_WEATHER = 44;

            int OPEN_WINDOW = 45;

            int CLOSE_WINDOW = 46;

            int SET_SLOT = 47;

            int WINDOW_ITEMS = 48;

            int WINDOW_DATA = 49;

            int TRANSACTION = 50;

            int UPDATE_SIGN = 51;

            int MAP = 52;

            int TILE_ENTITY_DATA = 53;

            int OPEN_SIGN_EDITOR = 54;

            int STATISTIC = 55;

            int PLAYER_INFO = 56;

            int ABILITIES = 57;

            int TAB_COMPLETE = 58;

            int SCOREBOARD_OBJECTIVE = 59;

            int SCOREBOARD_SCORE = 60;

            int SCOREBOARD_DISPLAY_OBJECTIVE = 61;

            int SCOREBOARD_TEAM = 62;

            int CUSTOM_PAYLOAD = 63;

            int KICK_DISCONNECT = 64;

            int SERVER_DIFFICULTY = 65;

            int COMBAT_EVENT = 66;

            int CAMERA = 67;

            int WORLD_BOARDER = 68;

            int TITLE = 69;

            int SET_COMPRESSION = 70;

            int PLAYER_LIST_HEADER_FOOTER = 71;

            int RESOURCE_PACK_SEND = 72;

            int UPDATE_ENTITY_NBT = 73;
        }

        interface Status {

            int SERVER_INFO = 0;

            int PONG = 1;
        }

        interface Login {

            int DISCONNECT = 0;

            int ENCRYPTION_BEGIN = 1;

            int SUCCESS = 2;

            int SET_COMPRESSION = 3;
        }
    }

    interface ServerBound {

        interface Handshaking {

            int SET_PROTOCOL = 0;
        }

        interface Play {

            int KEEP_ALIVE = 0;

            int CHAT = 1;

            int USE_ENTITY = 2;

            int FLYING = 3;

            int POSITION = 4;

            int LOOK = 5;

            int POSITION_LOOK = 6;

            int BLOCK_DIG = 7;

            int BLOCK_PLACE = 8;

            int HELD_ITEM_SLOT = 9;

            int ARM_ANIMATION = 10;

            int ENTITY_ACTION = 11;

            int STEER_VEHICLE = 12;

            int CLOSE_WINDOW = 13;

            int WINDOW_CLICK = 14;

            int TRANSACTION = 15;

            int SET_CREATIVE_SLOT = 16;

            int ENCHANT_ITEM = 17;

            int UPDATE_SIGN = 18;

            int ABILITIES = 19;

            int TAB_COMPLETE = 20;

            int SETTINGS = 21;

            int CLIENT_COMMAND = 22;

            int CUSTOM_PAYLOAD = 23;

            int SPECTATE = 24;

            int RESOURCE_PACK_STATUS = 25;
        }

        interface Status {

            int START = 0;

            int PING = 1;
        }

        interface Login {

            int START = 0;

            int ENCRYPTION_BEGIN = 1;
        }

    }
}