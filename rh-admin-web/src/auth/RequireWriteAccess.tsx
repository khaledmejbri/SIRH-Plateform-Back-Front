import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { getToken } from '../api/auth';
import { isDirectionOnly } from './jwtRoles';

/** Routes d'édition masquées pour DIRECTION (structure, unités). */
export default function RequireWriteAccess() {
  const loc = useLocation();
  const token = getToken();
  if (isDirectionOnly(token)) {
    return <Navigate to="/app/acces-refuse" replace state={{ from: loc.pathname }} />;
  }
  return <Outlet />;
}
