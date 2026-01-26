import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  loolService,
  CreateDocumentPayload,
} from "../api/lool.service";

/**
 * Hook to fetch provider context
 */
export const useProviderContext = () => {
  return useQuery({
    queryKey: ["lool", "provider-context"],
    queryFn: () => loolService.getProviderContext(),
  });
};

/**
 * Hook to create a new document
 */
export const useCreateDocument = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateDocumentPayload) =>
      loolService.createDocument(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["lool"] });
    },
  });
};
