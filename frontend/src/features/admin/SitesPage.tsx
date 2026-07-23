import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ChevronRight, MapPin, Plus } from 'lucide-react';
import { toast } from 'sonner';
import { useCreateSite, useSites } from '@/api/queries';
import { ROUTES } from '@/constants/routes';
import { EmptyBlock, ErrorBlock } from '@/components/common/StateViews';
import { CardListSkeleton } from '@/components/common/CardListSkeleton';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
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
import { formatNumber } from '@/lib/format';

const schema = z.object({
  siteCode: z.string().min(1, 'Required'),
  siteName: z.string().min(1, 'Required'),
  ownerName: z.string().min(1, 'Required'),
  location: z.string().min(1, 'Required'),
  totalAcres: z.coerce.number().positive('Must be greater than 0'),
});
type FormValues = z.infer<typeof schema>;

function AddSiteDialog() {
  const [open, setOpen] = useState(false);
  const createSite = useCreateSite();
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      siteCode: '',
      siteName: '',
      ownerName: '',
      location: '',
      totalAcres: 0,
    },
  });

  const onSubmit = async (v: FormValues) => {
    try {
      await createSite.mutateAsync(v);
      toast.success('Site created');
      form.reset();
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not create site');
    }
  };

  const fields: { name: keyof FormValues; label: string; type?: string }[] = [
    { name: 'siteCode', label: 'Site code' },
    { name: 'siteName', label: 'Site name' },
    { name: 'ownerName', label: 'Owner name' },
    { name: 'location', label: 'Location' },
    { name: 'totalAcres', label: 'Total acres', type: 'number' },
  ];

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Plus className="mr-1 h-4 w-4" /> Add site
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90svh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Add site</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            {fields.map((f) => (
              <FormField
                key={f.name}
                control={form.control}
                name={f.name}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{f.label}</FormLabel>
                    <FormControl>
                      <Input
                        type={f.type ?? 'text'}
                        step={f.type === 'number' ? '0.01' : undefined}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            ))}
            <Button
              type="submit"
              className="w-full"
              disabled={createSite.isPending}
            >
              {createSite.isPending ? 'Creating…' : 'Create site'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

export function SitesPage() {
  const { data, isLoading, isError, refetch } = useSites();
  const navigate = useNavigate();

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Sites</h2>
        <AddSiteDialog />
      </div>

      {isLoading && <CardListSkeleton />}
      {isError && (
        <ErrorBlock message="Could not load sites." onRetry={() => refetch()} />
      )}
      {data && data.length === 0 && (
        <EmptyBlock icon={<MapPin className="h-6 w-6" />} title="No sites" />
      )}

      <div className="space-y-3">
        {data?.map((site) => (
          <Card
            key={site.id}
            role="button"
            tabIndex={0}
            onClick={() => navigate(ROUTES.adminSiteDetail(site.id))}
            onKeyDown={(e) => {
              if (e.key === 'Enter') navigate(ROUTES.adminSiteDetail(site.id));
            }}
            className="cursor-pointer transition-colors hover:border-primary/40"
          >
            <CardContent className="flex items-start gap-3 p-4">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <MapPin className="h-5 w-5" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{site.siteName}</p>
                <p className="truncate text-xs text-muted-foreground">
                  {site.siteCode}
                  {site.location && ` · ${site.location}`}
                </p>
                <p className="text-xs text-muted-foreground">
                  {site.ownerName ? `Owner: ${site.ownerName}` : ''}
                  {site.totalAcres != null &&
                    ` · ${formatNumber(site.totalAcres)} acres`}
                </p>
              </div>
              <Badge variant={site.isActive ? 'default' : 'secondary'}>
                {site.isActive ? 'Active' : 'Inactive'}
              </Badge>
              <ChevronRight className="h-5 w-5 text-muted-foreground" />
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
