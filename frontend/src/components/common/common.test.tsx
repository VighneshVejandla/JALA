import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Wheat } from 'lucide-react';
import { EmptyBlock, ErrorBlock, LoadingBlock } from './StateViews';
import { StatCard } from './StatCard';
import { SiteSelector } from './SiteSelector';
import { FullScreenLoader } from './FullScreenLoader';
import * as fx from '@/test/fixtures';

describe('StateViews', () => {
  it('LoadingBlock renders a label', () => {
    render(<LoadingBlock label="Fetching" />);
    expect(screen.getByText('Fetching')).toBeInTheDocument();
  });

  it('ErrorBlock invokes onRetry', async () => {
    const onRetry = vi.fn();
    render(<ErrorBlock message="Broke" onRetry={onRetry} />);
    await userEvent.setup().click(screen.getByRole('button', { name: /try again/i }));
    expect(onRetry).toHaveBeenCalledOnce();
  });

  it('ErrorBlock without onRetry hides the button', () => {
    render(<ErrorBlock message="Broke" />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('EmptyBlock renders description and action', () => {
    render(
      <EmptyBlock
        title="Nothing"
        description="No rows"
        action={<button type="button">Add</button>}
      />,
    );
    expect(screen.getByText('No rows')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add' })).toBeInTheDocument();
  });
});

describe('StatCard', () => {
  it.each(['default', 'warning', 'success'] as const)('renders %s tone', (tone) => {
    render(<StatCard label="Feed" value="10 kg" icon={Wheat} hint="today" tone={tone} />);
    expect(screen.getByText('Feed')).toBeInTheDocument();
    expect(screen.getByText('10 kg')).toBeInTheDocument();
    expect(screen.getByText('today')).toBeInTheDocument();
  });
});

describe('SiteSelector', () => {
  it('renders nothing without sites', () => {
    const { container } = render(
      <SiteSelector sites={[]} siteId={null} onSelect={() => {}} />,
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('renders a trigger when sites exist', () => {
    render(
      <SiteSelector sites={fx.sites} siteId="site-1" onSelect={() => {}} />,
    );
    expect(screen.getByText(/North Farm/)).toBeInTheDocument();
  });
});

describe('FullScreenLoader', () => {
  it('renders the label', () => {
    render(<FullScreenLoader label="Please wait" />);
    expect(screen.getByText('Please wait')).toBeInTheDocument();
  });
});
