import api from "@/app/api/axios";

export interface PassengerDTO {
  id: number;
  name: string;
  surname: string;
  email: string;
  loyaltyPoints?: number;
}

export interface PagedResponse<T> {
  content?: T[];
  totalElements?: number;
  totalPages?: number;
  size?: number;
  number?: number;
}

export const getPassengers = async (
  page = 0,
  size = 100
): Promise<PassengerDTO[]> => {
  const res = await api.get<PagedResponse<PassengerDTO> | PassengerDTO[]>(
    `/passengers?page=${page}&size=${size}`
  );

  const data = res.data;

  if (!Array.isArray(data) && data && "content" in data) {
    const typedData = data as PagedResponse<PassengerDTO>;
    return typedData.content ?? [];
  }

  if (Array.isArray(data)) {
    return data as PassengerDTO[];
  }

  return [];
};

export const getPassengerById = async (id: number): Promise<PassengerDTO> => {
  const res = await api.get<PassengerDTO>(`/passengers/${id}`);
  return res.data;
};

export const createPassenger = async (
  data: PassengerDTO
): Promise<PassengerDTO> => {
  const res = await api.post<PassengerDTO>("/passengers", data);
  return res.data;
};

export const updatePassenger = async (
  id: number,
  data: PassengerDTO
): Promise<PassengerDTO> => {
  const res = await api.put<PassengerDTO>(`/passengers/${id}`, data);
  return res.data;
};

export const deletePassenger = async (id: number): Promise<void> => {
  await api.delete(`/passengers/${id}`);
};

export const checkEmailUnique = async (email: string): Promise<boolean> => {
  const res = await api.get<boolean>(`/passengers/check-email?email=${email}`);
  return res.data;
};

export const updateLoyaltyPoints = async (
  id: number,
  delta: number
): Promise<void> => {
  await api.patch(`/passengers/${id}/loyalty?delta=${delta}`);
};
