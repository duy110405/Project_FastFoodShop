import apiClient from './apiClient';

export const getAdminDashboard = async ({ fromDate, toDate, topN = 9 } = {}) => {
  const params = { topN };

  if (fromDate) params.fromDate = fromDate;
  if (toDate) params.toDate = toDate;

  const response = await apiClient.get('/admin/dashboard', { params });
  return response.data?.data;
};

