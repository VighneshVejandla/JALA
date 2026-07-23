import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Pencil } from 'lucide-react';
import { toast } from 'sonner';
import { useUpdatePond } from '@/api/queries';
import type { PondResponse } from '@/api/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';

const schema = z.object({
  pondCode: z.string().min(1, 'Required'),
  pondName: z.string().min(1, 'Required'),
  pondAcres: z.coerce.number().positive('Must be greater than 0'),
});
type FormValues = z.infer<typeof schema>;

export function PondEditButton({ pond }: { pond: PondResponse }) {
  const [open, setOpen] = useState(false);
  const updatePond = useUpdatePond(pond.siteId);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      pondCode: pond.pondCode,
      pondName: pond.pondName,
      pondAcres: pond.pondAcres ?? 0,
    },
  });

  const onSubmit = async (v: FormValues) => {
    try {
      await updatePond.mutateAsync({ id: pond.id, body: v });
      toast.success('Pond updated');
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not update pond');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" aria-label="Edit pond">
          <Pencil className="mr-1 h-4 w-4" /> Edit
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit pond</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="pondCode"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Pond code</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="pondName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Pond name</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="pondAcres"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Acres</FormLabel>
                  <FormControl>
                    <Input type="number" step="0.01" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" className="w-full" disabled={updatePond.isPending}>
              {updatePond.isPending ? 'Saving…' : 'Save changes'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
