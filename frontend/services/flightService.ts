import api from "@/app/api/axios";

export interface FlightDTO {
  id: number;
  flightNumber: string;
  origin: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  basePrice: number;
  capacity: number;
  bookedSeats?: number;
  airlineId: number;
  airlineName?: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getFlights = async (
  page = 0,
  size = 10,
  sort?: string
): Promise<PagedResponse<FlightDTO>> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });

  if (sort && sort.trim() !== "") {
    params.append("sort", sort);
  }

  const res = await api.get<PagedResponse<FlightDTO>>(
    `/flights?${params.toString()}`
  );
  return res.data;
};

export const getFlightById = async (id: number): Promise<FlightDTO> => {
  const res = await api.get<FlightDTO>(`/flights/${id}`);
  return res.data;
};

export const createFlight = async (data: FlightDTO): Promise<FlightDTO> => {
  const res = await api.post<FlightDTO>("/flights", data);
  return res.data;
};

export const updateFlight = async (
  id: number,
  data: FlightDTO
): Promise<FlightDTO> => {
  const res = await api.put<FlightDTO>(`/flights/${id}`, data);
  return res.data;
};

export const deleteFlight = async (id: number): Promise<void> => {
  await api.delete(`/flights/${id}`);
};
