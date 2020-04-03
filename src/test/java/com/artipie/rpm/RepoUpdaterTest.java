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

import com.artipie.asto.Key;
import com.artipie.asto.fs.FileStorage;
import io.reactivex.Observable;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Stub for actual RepoUpdater test. Does not perform any checks.
 * @since 0.4
 */
public final class RepoUpdaterTest {
    /**
     * Temporary file.
     */
    private RepoUpdater updater;

    @Test
    public void processSeveralPackages() throws IOException, XMLStreamException {
        Observable.fromArray(
            "aom-1.0.0-8.20190810git9666276.el8.aarch64.rpm",
            "nginx-1.16.1-1.el8.ngx.x86_64.rpm"
        ).flatMapCompletable(
            key -> {
                final Pkg pkg = this.pkg(key);
                return this.updater.processNext(new Key.From(key), pkg);
            }
        ).andThen(this.updater.complete(Key.ROOT)).blockingAwait();
    }

    @BeforeEach
    void setUp(@TempDir final Path folder) throws IOException, XMLStreamException {
        final Vertx vertx = Vertx.vertx();
        this.updater = new RepoUpdater(
            new FileStorage(folder, vertx.fileSystem()),
            vertx.fileSystem(),
            new NamingPolicy.HashPrefixed(Digest.SHA256),
            Digest.SHA256
        );
    }

    private Pkg pkg(final String name) throws URISyntaxException {
        return new Pkg(
            Paths.get(
                PrimaryProcessorTest.class.getResource(
                    String.format("/%s", name)
                ).toURI()
            )
        );
    }

}