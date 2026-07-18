package com.jala.backend.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestUtilTest {

    @Test
    void nullParamsDefaultToFirstPageWithMaxSize() {
        Pageable pageable = PageRequestUtil.of(null, null);

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(500);
    }

    @Test
    void explicitValuesWithinBoundsAreKept() {
        Pageable pageable = PageRequestUtil.of(2, 50);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(50);
    }

    @Test
    void sizeIsCappedAtMaxPageSize() {
        Pageable pageable = PageRequestUtil.of(0, 10_000);

        assertThat(pageable.getPageSize())
                .isEqualTo(PageRequestUtil.MAX_PAGE_SIZE)
                .isEqualTo(500);
    }

    @Test
    void sizeAtExactlyMaxIsAllowed() {
        Pageable pageable = PageRequestUtil.of(0, 500);

        assertThat(pageable.getPageSize()).isEqualTo(500);
    }

    @Test
    void negativePageIsClampedToZero() {
        Pageable pageable = PageRequestUtil.of(-3, 10);

        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void sizeBelowOneIsClampedToOne() {
        assertThat(PageRequestUtil.of(0, 0).getPageSize()).isEqualTo(1);
        assertThat(PageRequestUtil.of(0, -5).getPageSize()).isEqualTo(1);
    }

    @Test
    void nullPageWithExplicitSize() {
        Pageable pageable = PageRequestUtil.of(null, 25);

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(25);
    }

    @Test
    void explicitPageWithNullSize() {
        Pageable pageable = PageRequestUtil.of(4, null);

        assertThat(pageable.getPageNumber()).isEqualTo(4);
        assertThat(pageable.getPageSize()).isEqualTo(500);
    }
}
