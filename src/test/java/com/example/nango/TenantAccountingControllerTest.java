package com.example.nango;

import com.example.nango.client.exception.NangoConnectionNotFoundException;
import com.example.nango.client.model.NangoConnectSessionResponse;
import com.example.nango.client.model.NangoConnectSessionResponse.SessionData;
import com.example.nango.controller.GlobalIntegrationExceptionHandler;
import com.example.nango.controller.TenantAccountingController;
import com.example.nango.quickbooks.model.QuickBooksCompanyInfo;
import com.example.nango.quickbooks.model.QuickBooksCustomer;
import com.example.nango.quickbooks.model.QuickBooksInvoice;
import com.example.nango.quickbooks.service.QuickBooksAccountingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TenantAccountingController.class)
@Import(GlobalIntegrationExceptionHandler.class)
class TenantAccountingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuickBooksAccountingService accountingService;

    @Test
    @DisplayName("POST /connect returns connect session data")
    void testInitiateConnection() throws Exception {
        SessionData sessionData = new SessionData(
                "token_123",
                "https://connect.nango.dev/?session_token=token_123",
                Instant.now().plusSeconds(1800)
        );
        when(accountingService.initiateAccountingConnection(eq("tenant-42")))
                .thenReturn(new NangoConnectSessionResponse(sessionData));

        mockMvc.perform(post("/api/tenants/tenant-42/accounting/connect")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token_123"))
                .andExpect(jsonPath("$.connect_link").value(containsString("token_123")));
    }

    @Test
    @DisplayName("GET /company returns QuickBooks company info")
    void testGetCompanyInfo() throws Exception {
        QuickBooksCompanyInfo companyInfo = new QuickBooksCompanyInfo(
                "Sandbox Company US c56a",
                "Sandbox Company US c56a",
                new QuickBooksCompanyInfo.Address("123 Sierra Way", "San Pablo", "CA", "87999"),
                "US",
                "January",
                "QuickBooks Online Plus"
        );
        when(accountingService.getCompanyInfo(eq("tenant-42")))
                .thenReturn(companyInfo);

        mockMvc.perform(get("/api/tenants/tenant-42/accounting/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.CompanyName").value("Sandbox Company US c56a"))
                .andExpect(jsonPath("$.Country").value("US"));
    }

    @Test
    @DisplayName("GET /invoices returns list of QuickBooks invoices")
    void testGetInvoices() throws Exception {
        QuickBooksInvoice mockInvoice = new QuickBooksInvoice(
                "130",
                "1037",
                "2026-10-03",
                "2026-11-02",
                new BigDecimal("314.28"),
                new BigDecimal("314.28"),
                new QuickBooksInvoice.Reference("17", "Mark Cho"),
                new QuickBooksInvoice.Email("Mark@Cho.com"),
                Collections.emptyList()
        );
        when(accountingService.getInvoices(eq("tenant-42"), eq(10)))
                .thenReturn(List.of(mockInvoice));

        mockMvc.perform(get("/api/tenants/tenant-42/accounting/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].DocNumber").value("1037"))
                .andExpect(jsonPath("$[0].TotalAmt").value(314.28));
    }

    @Test
    @DisplayName("Unconnected tenant returns 404 with ProblemDetail")
    void testUnconnectedTenant() throws Exception {
        when(accountingService.getCompanyInfo(eq("unconnected-tenant")))
                .thenThrow(new NangoConnectionNotFoundException("Tenant has not connected a QuickBooks account."));

        mockMvc.perform(get("/api/tenants/unconnected-tenant/accounting/company"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Accounting Connection Not Found"))
                .andExpect(jsonPath("$.detail").value(containsString("not connected")));
    }
}
