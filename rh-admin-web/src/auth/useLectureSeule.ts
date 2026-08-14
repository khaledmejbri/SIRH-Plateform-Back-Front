import { useOutletContext } from 'react-router-dom';
import { getToken } from '../api/auth';
import { isDirectionOnly } from './jwtRoles';

type ShellOutlet = { lectureSeule?: boolean };

/** Direction (sans RH/ADMIN) : UI en consultation seule. */
export function useLectureSeule(): boolean {
  const ctx = useOutletContext<ShellOutlet | null>();
  if (ctx?.lectureSeule != null) return ctx.lectureSeule;
  return isDirectionOnly(getToken());
}
