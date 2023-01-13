/*
 * MIT License
 *
 * Copyright (c) 2021-2023 jhnc-oss
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

package io.jhnc.jenkins.plugins.statusoverview;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class ValidationUtilsTest {

    @Test
    void validUrlReturnsFalseOnNullOrEmpty() {
        assertThat(ValidationUtils.isValidUrl(null)).isFalse();
        assertThat(ValidationUtils.isValidUrl("")).isFalse();
        assertThat(ValidationUtils.isValidUrl("  ")).isFalse();
        assertThat(ValidationUtils.isValidUrl(" \t  ")).isFalse();
    }

    @Test
    void validUrlReturnsFalseOnInvalidUrl() {
        assertThat(ValidationUtils.isValidUrl("https://this.is inval.id/url/")).isFalse();
        assertThat(ValidationUtils.isValidUrl("://this.isinval.id/url/")).isFalse();
        assertThat(ValidationUtils.isValidUrl("http:/ /this.isinval.id/url/")).isFalse();
    }

    @Test
    void validUrlReturnsFalseOnLeadingBlanks() {
        assertThat(ValidationUtils.isValidUrl(" https://x.y")).isFalse();
        assertThat(ValidationUtils.isValidUrl("    https://x.y")).isFalse();
        assertThat(ValidationUtils.isValidUrl("\thttps://x.y")).isFalse();
        assertThat(ValidationUtils.isValidUrl(" \t https://x.y")).isFalse();
    }

    @Test
    void validUrlReturnsTrueOnValidUrl() {
        assertThat(ValidationUtils.isValidUrl("http://valid.url")).isTrue();
        assertThat(ValidationUtils.isValidUrl("https://valid.url")).isTrue();
        assertThat(ValidationUtils.isValidUrl("https://val-id.url.org")).isTrue();
        assertThat(ValidationUtils.isValidUrl("https://validurl.org/a/b/c")).isTrue();
        assertThat(ValidationUtils.isValidUrl("https://valid.url")).isTrue();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void isNullOrEmpty() {
        assertThat(ValidationUtils.isNullOrEmpty(null)).isTrue();
        assertThat(ValidationUtils.isNullOrEmpty("")).isTrue();
        assertThat(ValidationUtils.isNullOrEmpty(" ")).isFalse();
        assertThat(ValidationUtils.isNullOrEmpty("not empty")).isFalse();
        assertThat(ValidationUtils.isNullOrEmpty(" not empty ")).isFalse();
    }
}