import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

class ApiService {
    //create a new portfolio
    async createPortfolio(userId, name, initialCash) {
        try {
            const response = await axios.post(`${API_BASE_URL}/portfolios/create`, {
               params: {
                userId: userId,
                name: name,
                initialCash: initialCash
               }
            });
            return response.data;
        } catch (error) {
            console.error('Error creating portfolio:', error);
            throw error;
        }
    }

    // get portfolio value
    async getPortfolioValue(portfolioId) {
        try {
            const response = await axios.post(`${API_BASE_URL}/portfolios/create`, null, {
                params: {
                    userId: userId, 
                    name: name,
                    initialCash: initialCash
                }
            });
            return response.data;
        } catch (error) {
            console.error('Error fetching portfolio value:', error);
            throw error;
        }
    }
    async getPortfolioValue(portfolioId) {
        try {
            const response = await axios.get(`${API_BASE_URL}/portfolios/${portfolioId}/value`);
            return response.data;
        } catch (error) {
            console.error('Error fetching portfolio value:', error);
            throw error;
        }
    }
    // Get portfolio performance metrics
  async getPortfolioPerformance(portfolioId) {
    try {
      const response = await axios.get(`${BASE_URL}/portfolios/${portfolioId}/performance`);
      return response.data; // Returns performance map
    } catch (error) {
      console.error('Error getting performance:', error);
      throw error;
    }
  }

  // Update cash balance
  async updateCashBalance(portfolioId, amount, reason) {
    try {
      const response = await axios.put(`${BASE_URL}/portfolios/${portfolioId}/cash`, null, {
        params: {
          amount: amount,
          reason: reason
        }
      });
      return response.data; // Returns updated portfolio
    } catch (error) {
      console.error('Error updating cash:', error);
      throw error;
    }
  }
}

export default new ApiService();
