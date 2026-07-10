import React from 'react';
import ReactDOM from 'react-dom/client';
import "./style/index.css";
import App from "./App";
import { BrowserRouter } from 'react-router-dom';


import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {AuthProvider} from "./layout/Auth/ AuthProvider";

const queryClient = new QueryClient()


ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <BrowserRouter>
            <QueryClientProvider client={queryClient}>
                <AuthProvider>
                    <App />
                </AuthProvider>
            </QueryClientProvider>
        </BrowserRouter>
    </React.StrictMode>
);
