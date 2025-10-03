import React from "react";
import { BrowserRouter as Router } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { NotificationProvider } from "./context/NotificationContext";
import { BlockProvider } from "./context/BlockContext";
import AppRoutes from "./components/AppRoutes";
import ModalContainer from "./components/ModalContainer";
import ToastContainer from "./components/ToastContainer";
import ConfirmDialogContainer from "./components/ConfirmDialogContainer";
import AnimatedBackground from "./components/AnimatedBackground";
import ScrollToTop from "./components/ScrollToTop";
import "./styles/index.css";

function App() {
  return (
    <AuthProvider>
      <NotificationProvider>
        <BlockProvider>
          <Router>
            <ScrollToTop />
            <div className="App">
              <AnimatedBackground />
              <AppRoutes />
              <ModalContainer />
              <ToastContainer />
              <ConfirmDialogContainer />
            </div>
          </Router>
        </BlockProvider>
      </NotificationProvider>
    </AuthProvider>
  );
}

export default App;
