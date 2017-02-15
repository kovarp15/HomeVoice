/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 31.01.2017.
 * Copyright (c) 2017 Petr Kovář
 *
 * All rights reserved
 * kovarp15@fel.cvut.cz
 * HomeVoice for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HomeVoice for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HomeVoice for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.kovar.petr.homevoice.zwave.network.auth;

public class LocalAuth {
    private int default_ui;
    private String login;
    private String password;
    private Boolean keepme;
    private Boolean form;

    public LocalAuth () {}
    public LocalAuth (Boolean form,
                      String login, String password,
                      Boolean keepme, Integer default_ui)
    {
        this.default_ui = default_ui;
        this.login = login;
        this.password = password;
        this.keepme = keepme;
        this.form = form;
    }
}
