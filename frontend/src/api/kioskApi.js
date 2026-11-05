import {
    http,
    mutate
} from "./http";

function data(response) {
    return response.data;
}

function query(params = {}) {
    return Object.fromEntries(
        Object.entries(params)
            .filter(
                ([, value]) =>
                    value !== undefined
                    && value !== null
                    && value !== ""
            )
    );
}

function printRequestFormData(
    request,
    files
) {
    const formData =
        new FormData();

    formData.append(
        "request",
        new Blob(
            [
                JSON.stringify(request)
            ],
            {
                type: "application/json"
            }
        )
    );

    files.forEach(
        file =>
            formData.append(
                "files",
                file
            )
    );

    return formData;
}

export const authApi = {

    csrf: () =>
        http
            .get(
                "/api/auth/csrf",
                {
                    skipAuthRefresh: true
                }
            )
            .then(data),

    login: credentials =>
        mutate(
            "post",
            "/api/auth/login",
            credentials
        ).then(data),

    me: (config = {}) =>
        http
            .get(
                "/api/auth/me",
                config
            )
            .then(data),

    refresh: () =>
        mutate(
            "post",
            "/api/auth/refresh",
            null
        ).then(data),

    logout: () =>
        mutate(
            "post",
            "/api/auth/logout",
            null
        ).then(data),

    changePassword: request =>
        mutate(
            "patch",
            "/api/auth/password",
            request
        ).then(data),

    registerAgent: (
        request,
        idCard
    ) => {
        const formData =
            new FormData();

        formData.append(
            "fullName",
            request.fullName
        );

        formData.append(
            "agentId",
            request.agentId
        );

        formData.append(
            "email",
            request.email
        );

        formData.append(
            "contactNumber",
            request.contactNumber
        );

        formData.append(
            "password",
            request.password
        );

        formData.append(
            "confirmPassword",
            request.confirmPassword
        );

        formData.append(
            "idCard",
            idCard
        );

        return mutate(
            "post",
            "/api/auth/agents/register",
            formData
        ).then(data);
    }
};

export const publicApi = {

    pricing: () =>
        http
            .get(
                "/api/print-requests/pricing"
            )
            .then(data),

    previewPrintRequest: (
        request,
        files
    ) =>
        mutate(
            "post",
            "/api/print-requests/preview",
            printRequestFormData(
                request,
                files
            )
        ).then(data),

    createPrintRequest: (
        request,
        files
    ) =>
        mutate(
            "post",
            "/api/print-requests",
            printRequestFormData(
                request,
                files
            )
        ).then(data),

    getRequest: trackingNumber =>
        http
            .get(
                `/api/print-requests/${encodeURIComponent(
                    trackingNumber
                )}`
            )
            .then(data),

    cancelRequest: (
        trackingNumber,
        request
    ) =>
        mutate(
            "post",
            `/api/print-requests/${encodeURIComponent(
                trackingNumber
            )}/cancel`,
            request
        ).then(data),

    queue: params =>
        http
            .get(
                "/api/print-requests/queue",
                {
                    params: query(params)
                }
            )
            .then(data),

    paymentOptions: () =>
        http
            .get(
                "/api/print-requests/payment-options"
            )
            .then(data),

    cashPayment: (
        trackingNumber,
        request
    ) =>
        mutate(
            "post",
            `/api/print-requests/${encodeURIComponent(
                trackingNumber
            )}/payments/cash`,
            request
        ).then(data)
};

export const adminApi = {

    dashboard: (
        referenceDate
    ) =>
        http
            .get(
                "/api/admin/dashboard",
                {
                    params: query({
                        referenceDate
                    })
                }
            )
            .then(data),

    report: (
        period,
        referenceDate
    ) =>
        http
            .get(
                "/api/admin/reports",
                {
                    params: query({
                        period,
                        referenceDate
                    })
                }
            )
            .then(data),

    agentPerformance: (
        period,
        referenceDate
    ) =>
        http
            .get(
                "/api/admin/agents/performance",
                {
                    params: query({
                        period,
                        referenceDate
                    })
                }
            )
            .then(data),

    pendingAgents: () =>
        http
            .get(
                "/api/admin/agents/pending"
            )
            .then(data),

    approveAgent: accountId =>
        mutate(
            "patch",
            `/api/admin/agents/${accountId}/approve`,
            null
        ).then(data),

    rejectAgent: accountId =>
        mutate(
            "patch",
            `/api/admin/agents/${accountId}/reject`,
            null
        ).then(data),

    suspendAgent: accountId =>
        mutate(
            "patch",
            `/api/admin/agents/${accountId}/suspend`,
            null
        ).then(data),

    reactivateAgent: accountId =>
        mutate(
            "patch",
            `/api/admin/agents/${accountId}/reactivate`,
            null
        ).then(data),

    deleteAgent: accountId =>
        mutate(
            "delete",
            `/api/admin/agents/${accountId}`,
            null
        ).then(data),

    reopenRequest: (
        requestId,
        reason
    ) =>
        mutate(
            "post",
            `/api/admin/requests/${requestId}/reopen`,
            {
                reason
            }
        ).then(data)
};

export const agentApi = {

    dashboard: (
        referenceDate
    ) =>
        http
            .get(
                "/api/agent/dashboard",
                {
                    params: query({
                        referenceDate
                    })
                }
            )
            .then(data),

    queue: params =>
        http
            .get(
                "/api/agent/queue",
                {
                    params: query(params)
                }
            )
            .then(data),

    exceptions: () =>
        http
            .get(
                "/api/agent/queue/exceptions"
            )
            .then(data),

    claimNext: () =>
        mutate(
            "post",
            "/api/agent/queue/claim-next",
            null
        ).then(data),

    request: requestId =>
        http
            .get(
                `/api/agent/requests/${requestId}`
            )
            .then(data),

    cashPin: requestId =>
        mutate(
            "post",
            `/api/agent/requests/${requestId}/cash-pin`,
            null
        ).then(data),

    startPrinting: requestId =>
        mutate(
            "post",
            `/api/agent/requests/${requestId}/start-printing`,
            null
        ).then(data),

    complete: requestId =>
        mutate(
            "post",
            `/api/agent/requests/${requestId}/complete`,
            {
                confirmed: true
            }
        ).then(data),

    release: (
        requestId,
        reason
    ) =>
        mutate(
            "post",
            `/api/agent/requests/${requestId}/release`,
            {
                reason
            }
        ).then(data),

    pause: (
        requestId,
        reason
    ) =>
        mutate(
            "post",
            `/api/agent/requests/${requestId}/pause`,
            {
                reason
            }
        ).then(data),

    reportProblem: (
        requestId,
        exceptionType,
        reason
    ) =>
        mutate(
            "post",
            `/api/agent/requests/${requestId}/problem`,
            {
                exceptionType,
                reason
            }
        ).then(data),

    cancel: (
        requestId,
        reason
    ) =>
        mutate(
            "post",
            `/api/agent/requests/${requestId}/cancel`,
            {
                reason
            }
        ).then(data),

    reopen: (
        requestId,
        reason
    ) =>
        mutate(
            "post",
            `/api/agent/requests/${requestId}/reopen`,
            {
                reason
            }
        ).then(data),

    downloadFile: (
        requestId,
        fileId
    ) =>
        http.get(
            `/api/agent/requests/${requestId}/files/${fileId}/download`,
            {
                responseType: "blob"
            }
        ),

    pricing: () =>
        http
            .get(
                "/api/agent/pricing"
            )
            .then(data),

    updatePricing: request =>
        mutate(
            "patch",
            "/api/agent/pricing",
            request
        ).then(data),

    pricingHistory: params =>
        http
            .get(
                "/api/agent/pricing/history",
                {
                    params: query(params)
                }
            )
            .then(data)
};

export const inventoryApi = {

    categories: (
        includeArchived = false
    ) =>
        http
            .get(
                "/api/agent/inventory/categories",
                {
                    params: {
                        includeArchived
                    }
                }
            )
            .then(data),

    createCategory: name =>
        mutate(
            "post",
            "/api/agent/inventory/categories",
            {
                name
            }
        ).then(data),

    renameCategory: (
        categoryId,
        name
    ) =>
        mutate(
            "patch",
            `/api/agent/inventory/categories/${categoryId}/rename`,
            {
                name
            }
        ).then(data),

    archiveCategory: categoryId =>
        mutate(
            "patch",
            `/api/agent/inventory/categories/${categoryId}/archive`,
            null
        ).then(data),

    reactivateCategory: categoryId =>
        mutate(
            "patch",
            `/api/agent/inventory/categories/${categoryId}/reactivate`,
            null
        ).then(data),

    mergeCategory: (
        sourceId,
        destinationCategoryId
    ) =>
        mutate(
            "post",
            `/api/agent/inventory/categories/${sourceId}/merge`,
            {
                destinationCategoryId
            }
        ).then(data),

    deleteCategory: categoryId =>
        mutate(
            "delete",
            `/api/agent/inventory/categories/${categoryId}`,
            null
        ).then(data),

    items: params =>
        http
            .get(
                "/api/agent/inventory/items",
                {
                    params: query(params)
                }
            )
            .then(data),

    item: itemId =>
        http
            .get(
                `/api/agent/inventory/items/${itemId}`
            )
            .then(data),

    createItem: request =>
        mutate(
            "post",
            "/api/agent/inventory/items",
            request
        ).then(data),

    renameItem: (
        itemId,
        name
    ) =>
        mutate(
            "patch",
            `/api/agent/inventory/items/${itemId}/rename`,
            {
                name
            }
        ).then(data),

    changePrice: (
        itemId,
        price
    ) =>
        mutate(
            "patch",
            `/api/agent/inventory/items/${itemId}/price`,
            {
                price
            }
        ).then(data),

    archiveItem: itemId =>
        mutate(
            "patch",
            `/api/agent/inventory/items/${itemId}/archive`,
            null
        ).then(data),

    reactivateItem: itemId =>
        mutate(
            "patch",
            `/api/agent/inventory/items/${itemId}/reactivate`,
            null
        ).then(data),

    priceHistory: itemId =>
        http
            .get(
                `/api/agent/inventory/items/${itemId}/price-history`
            )
            .then(data),

    sale: itemId =>
        mutate(
            "post",
            `/api/agent/inventory/items/${itemId}/sale`,
            null
        ).then(data),

    returnItem: (
        itemId,
        reason
    ) =>
        mutate(
            "post",
            `/api/agent/inventory/items/${itemId}/return`,
            {
                reason
            }
        ).then(data),

    correction: (
        itemId,
        quantity,
        reason
    ) =>
        mutate(
            "post",
            `/api/agent/inventory/items/${itemId}/correction`,
            {
                quantity,
                reason
            }
        ).then(data),

    transactions: params =>
        http
            .get(
                "/api/agent/inventory/transactions",
                {
                    params: query(params)
                }
            )
            .then(data),

    myTransactions: params =>
        http
            .get(
                "/api/agent/inventory/transactions/my",
                {
                    params: query(params)
                }
            )
            .then(data)
};

export function pageContent(
    response
) {
    if (Array.isArray(response)) {
        return response;
    }

    return response?.content || [];
}