import api from "@/app/api/axios";

export interface AirlineDTO {
  id: number;
  codeIATA: string;
  codeICAO: string;
  name: string;
  country: string;
  fleetSize: string;
  flightIds?: number[];
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getAirlines = async (
  page = 0,
  size = 10
): Promise<PagedResponse<AirlineDTO>> => {
  const res = await api.get<PagedResponse<AirlineDTO>>(
    `/airlines?page=${page}&size=${size}`
  );
  return res.data;
};

export const getAirlineById = async (id: number): Promise<AirlineDTO> => {
  const res = await api.get<AirlineDTO>(`/airlines/${id}`);
  return res.data;
};

export const createAirline = async (data: AirlineDTO): Promise<AirlineDTO> => {
  const res = await api.post<AirlineDTO>("/airlines", data);
  return res.data;
};

export const updateAirline = async (
  id: number,
  data: AirlineDTO
): Promise<AirlineDTO> => {
  const res = await api.put<AirlineDTO>(`/airlines/${id}`, data);
  return res.data;
};

export const deleteAirline = async (id: number): Promise<void> => {
  await api.delete(`/airlines/${id}`);
};
