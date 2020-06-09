/*
 * MIT License
 *
 * Copyright (c) derrop and derklaro
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.derrop.proxy.plugins.gomme.match.event.global.player;

import com.github.derrop.proxy.plugins.gomme.match.event.MatchEvent;
import com.github.derrop.proxy.plugins.gomme.match.messages.MessageType;

public class PlayerLeaveInGameEvent extends MatchEvent {

    private final String name;
    private final MessageType team;
    private final int teamRemainingPlayers;

    public PlayerLeaveInGameEvent(String name, MessageType team, int teamRemainingPlayers) {
        this.name = name;
        this.team = team;
        this.teamRemainingPlayers = teamRemainingPlayers;
    }

    @Override
    public String toPlainText() {
        return "Player " + this.name + " left in the in game phase (previous team: " + team + "; remaining players: " + teamRemainingPlayers + ")";
    }

    public String getName() {
        return this.name;
    }

    public MessageType getTeam() {
        return this.team;
    }

    public int getTeamRemainingPlayers() {
        return this.teamRemainingPlayers;
    }
}
