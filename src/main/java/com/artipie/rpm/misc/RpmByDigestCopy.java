/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 artipie.com
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
package com.artipie.rpm.misc;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.ext.ContentDigest;
import com.artipie.asto.ext.Digests;
import com.artipie.asto.ext.KeyLastPart;
import com.artipie.asto.ext.PublisherAs;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Copy rpms from one storage to another filtering by digests.
 * @since 0.11
 */
public final class RpmByDigestCopy {

    /**
     * Storage to copy from.
     */
    private final Storage from;

    /**
     * Key to copy from.
     */
    private final Key key;

    /**
     * Content hex digests to exclude.
     */
    private final List<String> digests;

    /**
     * Digest algorithm.
     */
    private final Digests algorithm;

    /**
     * Ctor.
     * @param from Storage to copy from
     * @param key Key to copy from
     * @param digests Content digests to exclude
     * @param algorithm Digest algorithm
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public RpmByDigestCopy(
        final Storage from, final Key key, final List<String> digests,
        final Digests algorithm
    ) {
        this.from = from;
        this.digests = digests;
        this.key = key;
        this.algorithm = algorithm;
    }

    /**
     * Ctor.
     * @param from Storage to copy from
     * @param key Key to copy from
     * @param digests Content digests to exclude
     */
    public RpmByDigestCopy(final Storage from, final Key key, final List<String> digests) {
        this(from, key, digests, Digests.SHA256);
    }

    /**
     * Copy rpm to destination storage filtering by digest.
     * @param dest Destination
     * @return Completable copy operation
     */
    Completable copy(final Storage dest) {
        return SingleInterop.fromFuture(this.from.list(this.key))
            .flatMapPublisher(Flowable::fromIterable)
            .filter(item -> item.string().endsWith(".rpm"))
            .flatMapCompletable(
                rpm -> Completable.fromFuture(
                    this.from.value(rpm).thenCompose(content -> this.handleRpm(dest, rpm, content))
                )
            );
    }

    /**
     * Handle rpm: calc its digest and check whether it's present in digests list, save if to
     * storage if necessary.
     * @param dest Where to copy
     * @param rpm Rpm file key
     * @param content Rpm content
     * @return CompletionStage action
     */
    private CompletionStage<Void> handleRpm(
        final Storage dest, final Key rpm, final Content content
    ) {
        return new PublisherAs(content).bytes().thenCompose(
            source -> new ContentDigest(new Content.From(source), this.algorithm)
                .hex().thenCompose(
                    hex -> {
                        final CompletableFuture<Void> res;
                        if (this.digests.contains(hex)) {
                            res = CompletableFuture.allOf();
                        } else {
                            res = dest.save(
                                new Key.From(new KeyLastPart(rpm).get()), new Content.From(source)
                            );
                        }
                        return res;
                    }
                )
            );
    }
}
