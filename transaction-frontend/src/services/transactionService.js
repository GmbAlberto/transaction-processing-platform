import api from "./api";

const TRANSACTIONS_ENDPOINT = "/transactions";

export const transactionService = {
    async getTransactions({
        page = 0,
        size = 5,
        sortBy = "createdAt",
        direction = "desc",
    } = {}) {
        const response = await api.get(TRANSACTIONS_ENDPOINT, {
            params: {
                page,
                size,
                sortBy,
                direction,
            },
        });

        return response.data;
    },

    async createTransaction(transaction) {
        const response = await api.post(
            TRANSACTIONS_ENDPOINT,
            transaction
        );

        return response.data;
    },

    async cancelTransaction({ id, reference }) {
        const response = await api.patch(
            `${TRANSACTIONS_ENDPOINT}/cancel`,
            {
                id,
                reference,
            }
        );

        return response.data;
    },
};