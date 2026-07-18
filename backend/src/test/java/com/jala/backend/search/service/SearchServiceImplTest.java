package com.jala.backend.search.service;

import com.jala.backend.feedentry.entity.FeedEntry;
import com.jala.backend.feedentry.repository.FeedEntryRepository;
import com.jala.backend.harvest.entity.Harvest;
import com.jala.backend.harvest.repository.HarvestRepository;
import com.jala.backend.medicine.entity.MedicineEntry;
import com.jala.backend.medicine.repository.MedicineRepository;
import com.jala.backend.notification.entity.Notification;
import com.jala.backend.notification.repository.NotificationRepository;
import com.jala.backend.pond.entity.Pond;
import com.jala.backend.pond.repository.PondRepository;
import com.jala.backend.pondcycle.entity.PondCycle;
import com.jala.backend.search.dto.response.GlobalSearchResponse;
import com.jala.backend.site.entity.Site;
import com.jala.backend.site.repository.SiteRepository;
import com.jala.backend.siteaccess.service.SiteAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PondRepository pondRepository;

    @Mock
    private FeedEntryRepository feedEntryRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SiteAccessService siteAccessService;

    @InjectMocks
    private SearchServiceImpl service;

    private Site site;
    private Pond pond;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .id(UUID.randomUUID()).siteCode("S-001").siteName("Site 1").build();
        pond = Pond.builder()
                .id(UUID.randomUUID()).site(site).pondCode("P-001").pondName("Pond 1")
                .build();
    }

    private void stubAllSearches() {
        PondCycle cycle = PondCycle.builder()
                .id(UUID.randomUUID()).pond(pond).build();

        lenient().when(siteRepository.search(any(), any()))
                .thenReturn(List.of(site));
        lenient().when(pondRepository.search(any(), any()))
                .thenReturn(List.of(pond));
        lenient().when(feedEntryRepository.search(any(), any()))
                .thenReturn(List.of(FeedEntry.builder()
                        .id(UUID.randomUUID()).pondCycle(cycle).remarks("r").build()));
        lenient().when(medicineRepository.search(any(), any()))
                .thenReturn(List.of(MedicineEntry.builder()
                        .id(UUID.randomUUID()).pondCycle(cycle).build()));
        lenient().when(harvestRepository.search(any(), any()))
                .thenReturn(List.of(Harvest.builder()
                        .id(UUID.randomUUID()).pondCycle(cycle).buyerName("b").build()));
        lenient().when(notificationRepository.search(any(), any()))
                .thenReturn(List.of(Notification.builder()
                        .id(UUID.randomUUID()).siteId(site.getId())
                        .title("t").message("m").build()));
    }

    @Test
    @DisplayName("unrestricted user gets results from every category")
    void search_unrestricted() {
        when(siteAccessService.accessibleSiteIds()).thenReturn(null);
        stubAllSearches();

        GlobalSearchResponse result = service.search("keyword");

        assertThat(result.getSites()).hasSize(1);
        assertThat(result.getPonds()).hasSize(1);
        assertThat(result.getFeedEntries()).hasSize(1);
        assertThat(result.getMedicineEntries()).hasSize(1);
        assertThat(result.getHarvests()).hasSize(1);
        assertThat(result.getNotifications()).hasSize(1);
    }

    @Test
    @DisplayName("restricted user sees nothing outside their assigned sites")
    void search_restricted_filtersForeignSites() {
        // Accessible set does not include the entities' site.
        when(siteAccessService.accessibleSiteIds())
                .thenReturn(List.of(UUID.randomUUID()));
        stubAllSearches();

        GlobalSearchResponse result = service.search("keyword");

        assertThat(result.getSites()).isEmpty();
        assertThat(result.getPonds()).isEmpty();
        assertThat(result.getFeedEntries()).isEmpty();
        assertThat(result.getMedicineEntries()).isEmpty();
        assertThat(result.getHarvests()).isEmpty();
        assertThat(result.getNotifications()).isEmpty();
    }

    @Test
    @DisplayName("restricted user sees results from their assigned site")
    void search_restricted_keepsOwnSite() {
        when(siteAccessService.accessibleSiteIds())
                .thenReturn(List.of(site.getId()));
        lenient().when(siteRepository.search(eq("keyword"), any()))
                .thenReturn(List.of(site));
        stubAllSearches();

        GlobalSearchResponse result = service.search("keyword");

        assertThat(result.getSites()).hasSize(1);
        assertThat(result.getPonds()).hasSize(1);
    }
}
