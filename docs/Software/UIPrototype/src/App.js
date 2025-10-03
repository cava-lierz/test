import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import AppRoutes from './components/AppRoutes';
import ModalContainer from './components/ModalContainer';
import AnimatedBackground from './components/AnimatedBackground';
import ScrollToTop from './components/ScrollToTop';
import './styles/index.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <ScrollToTop />
        <div className="App">
          <AnimatedBackground />
          <AppRoutes />
          <ModalContainer />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
