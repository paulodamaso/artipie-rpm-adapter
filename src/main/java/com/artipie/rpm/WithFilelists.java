/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.rpm;

import io.reactivex.Completable;

/**
 * Rpm decorator which provide filelist creation on update.
 *
 * @since 0.1
 * @todo #17:30min Implement WithFilelists decorator.
 *  WithFilelists decorator create filelists.xml and filelists.xml.gz files on
 *  upload. Implement this behavior and then enable the test in
 *  WithFilelistsTest.
 */
public final class WithFilelists implements Rpm {

    /**
     * Original Rpm.
     */
    private final Rpm origin;

    /**
     * Constructor.
     *
     * @param rpm Rpm to be wrapped.
     */
    public WithFilelists(final Rpm rpm) {
        this.origin = rpm;
    }

    @Override
    public Completable update(final String key) {
        return this.origin.update(key);
    }

    @Override
    public Completable batchUpdate(final String repo) {
        return this.origin.batchUpdate(repo);
    }
}