/*
 * This file is a part of Telegram X
 * Copyright © 2014 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * File created on 17/11/2023
 */
package org.thunderdog.challegram.component.chat.filter;

import org.drinkless.tdlib.TdApi;

import java.util.HashSet;
import java.util.Set;

import me.vkryl.td.ChatId;
import me.vkryl.td.Td;

class Content {
  public final Set<String> mentionsUsername = new HashSet<>();
  public final Set<Long> mentionsId = new HashSet<>();
  public final Set<String> links = new HashSet<>();

  // private final Set<String> internalLinks = new HashSet<>();
  // private final Set<String> externalLinks = new HashSet<>();

  public Content () {}

  public void add (TdApi.MessageContent content) {
    final TdApi.FormattedText textOrCaption = Td.textOrCaption(content);
    if (textOrCaption == null || textOrCaption.text == null) return;

    if (textOrCaption.entities != null) {
      for (TdApi.TextEntity entity : textOrCaption.entities) {
        switch (entity.type.getConstructor()) {
          case TdApi.TextEntityTypeMention.CONSTRUCTOR: {
            mentionsUsername.add(Td.substring(textOrCaption.text, entity));
            break;
          }
          case TdApi.TextEntityTypeMentionName.CONSTRUCTOR: {
            mentionsId.add(ChatId.fromUserId(((TdApi.TextEntityTypeMentionName) entity.type).userId));
            break;
          }
          case TdApi.TextEntityTypeUrl.CONSTRUCTOR: {
            addLink(Td.substring(textOrCaption.text, entity));
            break;
          }
          case TdApi.TextEntityTypeTextUrl.CONSTRUCTOR: {
            addLink(((TdApi.TextEntityTypeTextUrl) entity.type).url);
            break;
          }
          default: {
            break;
          }
        }
      }
    }
  }

  private void addLink (String link) {
    links.add(link);
  }
}
