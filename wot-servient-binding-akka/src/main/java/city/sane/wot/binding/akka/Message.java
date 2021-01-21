/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.binding.akka;

import city.sane.wot.content.Content;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

public interface Message {
    abstract class ContentMessage implements Message {
        public final Content content;

        protected ContentMessage(Content content) {
            this.content = content;
        }

        ContentMessage() {
            // required by jackson
            content = null;
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ContentMessage that = (ContentMessage) o;
            return Objects.equals(content, that.content);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "content=" + content +
                    '}';
        }
    }

    abstract class InteractionMessage implements Message {
        public final String name;

        public InteractionMessage(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InteractionMessage that = (InteractionMessage) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    abstract class InteractionWithContentMessage extends ContentMessage {
        public final String name;

        public InteractionWithContentMessage(String name) {
            this(name, null);
        }

        public InteractionWithContentMessage(String name, Content content) {
            super(content);
            this.name = name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            InteractionWithContentMessage that = (InteractionWithContentMessage) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "content=" + content +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    abstract class ErrorMessage implements Message {
        public final Throwable e;

        protected ErrorMessage(Throwable e) {
            this.e = e;
        }

        ErrorMessage() {
            // required by jackson
            e = null;
        }

        @Override
        public int hashCode() {
            return Objects.hash(e);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ErrorMessage that = (ErrorMessage) o;
            return Objects.equals(e, that.e);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "e=" + e +
                    '}';
        }
    }

    class SubscriptionNext extends ContentMessage {
        public SubscriptionNext(Content content) {
            super(content);
        }
    }

    class SubscriptionError extends ErrorMessage {
        public SubscriptionError(Throwable e) {
            super(e);
        }
    }

    class SubscribeFailed extends ErrorMessage {
        public SubscribeFailed(Throwable e) {
            super(e);
        }
    }

    // https://stackoverflow.com/a/53845446/1074188
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    class SubscriptionComplete implements Message {
        public SubscriptionComplete() {
            // required by jackson
        }

        @Override
        public String toString() {
            return "SubscriptionComplete{}";
        }
    }
}
