import { LocationAuthorizationStatusIOS } from 'react-native-syrf-client';

export const timeFormat = (timeInMilis: number, format: string) => {
  const date = new Date(timeInMilis);
  const yyyy = date.getFullYear().toString();
  format = format.replace(/yyyy/g, yyyy);
  const MM = (date.getMonth() + 1).toString();
  format = format.replace(/MM/g, MM[1] ? MM : '0' + MM[0]);
  const dd = date.getDate().toString();
  format = format.replace(/dd/g, dd[1] ? dd : '0' + dd[0]);
  const hh = date.getHours().toString();
  format = format.replace(/hh/g, hh[1] ? hh : '0' + hh[0]);
  const mm = date.getMinutes().toString();
  format = format.replace(/mm/g, mm[1] ? mm : '0' + mm[0]);
  const ss = date.getSeconds().toString();
  format = format.replace(/ss/g, ss[1] ? ss : '0' + ss[0]);
  return format;
};

export const hasPermissionIOS = (status: LocationAuthorizationStatusIOS) => {
  return (
    status === LocationAuthorizationStatusIOS.AuthorizedAlways ||
    status === LocationAuthorizationStatusIOS.AuthorizedWhenInUse
  );
};
