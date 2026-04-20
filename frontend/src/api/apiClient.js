import axios from 'axios';

const apiClient = axios.create({
<<<<<<< HEAD

    baseURL: 'http://localhost:8080/api/v1',
=======
    baseURL: 'http://localhost:8080/api',
>>>>>>> c82d4a6b0c2eec27faed140f4105f10ff13dc4df
    withCredentials: true
});

export default apiClient;