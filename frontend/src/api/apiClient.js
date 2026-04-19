import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    baseURL: 'http://localhost:8081/api',
    withCredentials: true
});

export default apiClient;