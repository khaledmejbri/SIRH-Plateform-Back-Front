import { Outlet, useLocation } from 'react-router-dom';
import { getToken } from '../api/auth';
import AccesRefusePage from '../pages/AccesRefusePage';
import { canAccessBackoffice } from './jwtRoles';
import { Navigate } from 'react-router-dom';

export default function ProtectedRoute() {
  const loc = useLocation();
  const token = getToken();
  if (!token) {
    return <Navigate to="/login" replace state={{ from: loc.pathname }} />;
  }
  if (!canAccessBackoffice(token)) {
    return <AccesRefusePage />;
  }
  return <Outlet />;
}
